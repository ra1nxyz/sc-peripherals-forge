package io.sc3.peripherals.posters.printer

import dan200.computercraft.api.peripheral.IComputerAccess
import io.sc3.library.ext.optCompound
import io.sc3.library.networking.NetworkUtil.sendToAllTracking
import io.sc3.peripherals.Registration.ModBlockEntities.posterPrinter
import io.sc3.peripherals.Registration.ModItems
import io.sc3.peripherals.config.ScPeripheralsConfig.config
import io.sc3.peripherals.posters.PosterItem
import io.sc3.peripherals.posters.PosterItem.Companion.POSTER_KEY
import io.sc3.peripherals.posters.PosterPrintData
import io.sc3.peripherals.util.BaseBlockEntity
import io.sc3.peripherals.util.ImplementedInventory
import net.minecraft.world.MenuProvider
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.network.chat.Component
import net.minecraft.world.Containers
import net.minecraft.core.NonNullList
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PosterPrinterBlockEntity(
  blockPos: BlockPos,
  state: BlockState
) : BaseBlockEntity(posterPrinter, blockPos, state), MenuProvider, ImplementedInventory, WorldlyContainer {
  private val inventory = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY)

  /** Set of computers that are attached as a peripheral to the printer, so they may receive print state events. */
  val computers: MutableSet<IComputerAccess> = Collections.newSetFromMap(ConcurrentHashMap())

  var animatingPosterId: String? = null
  var animationStartTime: Long = 0
  var animationTicks: Long = 0

  var data: PosterPrintData = PosterPrintData(
    null, null, ByteArray(128*128)
  )
  var printing = false
    set(value) {
      val oldValue = field
      field = value
      printProgress = 0

      if (!oldValue && value) {
        PosterPrinterPeripheral.sendPrintStatusEvent(this)
      }
    }
  var printCount = 0
  private var outputStack: ItemStack = ItemStack.EMPTY

  var ink = 0
  var printProgress = 0
  // This property is synced from the server's config to the client
  var maxPrintProgress: Int = config.getOrElse("poster_printer.print_ticks", 100)

  private var inksDirty = false
  private var outputDirty = false
  private var dataDirty = false

  private val propertyDelegate: ContainerData = object : ContainerData {
    override fun get(index: Int): Int {
      return when (index) {
        1 -> ink
        2 -> printProgress
        3 -> maxPrintProgress
        else -> 0
      }
    }

    override fun set(index: Int, value: Int) {
      when(index) {
        1 -> ink = value
        2 -> printProgress = value
        3 -> maxPrintProgress = value
      }
    }

  fun size() = 4

    // version 1.20+ ContainerData requires getCount() instead of size(), why? i dont know
    override fun getCount(): Int = size()
  }

  override fun getItems(): NonNullList<ItemStack> = inventory


  // onBroken is not a standard override in Forge, converted to a normal function
  override fun onBroken() {
    super.onBroken()
    Containers.dropContents(level, blockPos, inventory)
    inventory.clear()
  }


  // isValid is not a standard override, but WorldlyContainer uses isItemValidForSlot in this functionality
  fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean = when(index) {
    PAPER_SLOT -> stack.is(Items.PAPER)
    INK_SLOT -> stack.is(ModItems.inkCartridge)
    else -> false
  }

  override fun getSlotsForFace(side: Direction): IntArray = when(side) {
    Direction.DOWN -> downSideSlots
    else -> otherSideSlots
  }

  override fun canPlaceItemThroughFace(index: Int, stack: ItemStack, direction: Direction?): Boolean =
    isItemValidForSlot(index, stack)

  override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean =
    !isItemValidForSlot(index, stack) // Allow extracting output items from any direction (? needs testing)

  private fun inkValue(stack: ItemStack) =
    if (stack.is(ModItems.inkCartridge)) inkValue else 0

  private fun canMergeOutput(): Boolean {
    val current = getStack(OUTPUT_SLOT)
    val output = PosterItem.create(level ?: return false, data)
    return current.isEmpty || ItemStack.canCombine(current, output)
  }

  fun canPrint(): Boolean {
    if (outputStack.isEmpty && canMergeOutput()) {
      val cost = data.computeCosts()
      val paperStack = getStack(PAPER_SLOT)
      if (ink >= cost && paperStack.count >= 1) {
        return true
      }
    }

    return false
  }

  fun onTick(level: Level) {
    try {
      if (level.isClientSide) return

      tickInputSlot()
      tickOutputSlot(level)

      if (inksDirty || outputDirty || dataDirty) {
        markDirty()
      }

      // Send ink update packets to any tracking entities
      if (inksDirty) {
        sendToAllTracking(level.getChunkAt(blockPos), PosterPrinterInkPacket(blockPos, ink))
        inksDirty = false
      }

      if (outputDirty) {
        outputDirty = false
      }

      // Send data update packets to any tracking entities
      if (dataDirty) {
//      sendToAllTracking(level.getChunkAt(blockPos), PrinterDataPacket(blockPos, data))
        dataDirty = false
      }

      val activelyPrinting = !outputStack.isEmpty
      if (activelyPrinting != cachedState.get(PosterPrinterBlock.printing)) {
        level.setBlock(blockPos, cachedState.setValue(PosterPrinterBlock.printing, activelyPrinting), 3)
      }

      val hasPaper = !getStack(PAPER_SLOT).isEmpty
      if (hasPaper != cachedState.get(PosterPrinterBlock.hasPaper)) {
        level.setBlock(blockPos, cachedState.setValue(PosterPrinterBlock.hasPaper, hasPaper), 3)
      }
    } catch (e: Exception) {
      logger.error("Error in poster printer tick", e)
    }
  }

  private fun tickInputSlot() {
    // The value of one item in this slot. Don't allow any item waste. Only process one item per tick.

    val inputInk = inkValue(getStack(INK_SLOT))
    if (inputInk > 0 && maxInk - ink >= inputInk) {
      val stack = removeStack(INK_SLOT, 1)
      if (!stack.isEmpty) {
        ink += inputInk
        inksDirty = true

        // Replace the ink cartridge with an empty cartridge
        setStack(INK_SLOT, ItemStack(ModItems.emptyInkCartridge))
      }
    }
  }

  private fun tickOutputSlot(world: Level) {
    // Printing logic
    if (printing && canPrint()) {
      val cost = data.computeCosts()
      val paperStack = getStack(PAPER_SLOT)

      // Start printing a single item and consume the inks
      ink -= cost
      inksDirty = true
      paperStack.decrement(1)

      printCount--
      outputStack = PosterItem.create(world, data)
  val posterId = outputStack.tag?.getString(POSTER_KEY) ?: ""
      data.posterId = posterId // Allow merging with the output stack
      if (printCount < 1) printing = false

      // Send animation packet to all tracking entities
      sendToAllTracking(world.getWorldChunk(pos), PosterPrinterStartPrintPacket(pos, posterId))

      outputDirty = true
    }

    if (!outputStack.isEmpty) {
      printProgress = (printProgress + 1).coerceAtMost(maxPrintProgress)

      if (printProgress >= maxPrintProgress) {
        val result = getStack(OUTPUT_SLOT)
        if (result.isEmpty) {
          setStack(OUTPUT_SLOT, outputStack)
        } else if (result.count < result.maxCount && canMergeOutput()) {
          result.count++
        } else {
          return
        }

        printProgress = 0
        outputStack = ItemStack.EMPTY
        outputDirty = true

        PosterPrinterPeripheral.sendPrintCompleteEvent(this)
        PosterPrinterPeripheral.sendPrintStatusEvent(this)
      }
    }
  }

  override fun createMenu(syncId: Int, inv: Inventory, player: Player): AbstractContainerMenu =
    PosterPrinterScreenHandler(syncId, inv, this, pos, propertyDelegate)

  override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
    buf.writeBlockPos(pos)
  }

  override fun getDisplayName(): Text = Text.translatable(cachedState.block.translationKey)

  override fun readNbt(nbt: NbtCompound) {
    super.readNbt(nbt)

    inventory.clear()
    Inventories.readNbt(nbt, inventory)

    data = PosterPrintData.fromNbt(nbt.getCompound("data"))
    printing = nbt.getBoolean("printing")
    printCount = nbt.getInt("printCount")
    outputStack = nbt.optCompound("outputStack")?.let { ItemStack.fromNbt(it) } ?: ItemStack.EMPTY

    ink = nbt.getInt("ink")
    printProgress = nbt.getInt("printProgress")
  }

  override fun writeNbt(nbt: NbtCompound) {
    super.writeNbt(nbt)

    Inventories.writeNbt(nbt, inventory)

    nbt.put("data", data.toNbt())
    nbt.putBoolean("printing", printing)
    nbt.putInt("printCount", printCount)
    nbt.put("outputStack", outputStack.writeNbt(NbtCompound()))

    nbt.putInt("ink", ink)
    nbt.putInt("printProgress", printProgress)
  }

  override fun toUpdatePacket(): Packet<ClientPlayPacketListener> =
    BlockEntityUpdateS2CPacket.create(this)

  override fun toInitialChunkDataNbt(): NbtCompound {
    val nbt = super.toInitialChunkDataNbt()
    writeNbt(nbt)
    nbt.remove("data") // Don't send the print data to the client

    return nbt
  }

  override fun markDirty() {
    super<BaseBlockEntity>.markDirty()
    getWorld()!!.updateListeners(getPos(), cachedState, cachedState, Block.NOTIFY_ALL)
  }

  fun dataUpdated() {
    val wasPrinting = printing
    printing = false
    dataDirty = true

    if (wasPrinting && !printing) {
      PosterPrinterPeripheral.sendPrintStatusEvent(this)
    }
  }

  val peripheral by lazy { PosterPrinterPeripheral(this) }

  companion object {
    private val logger = LoggerFactory.getLogger(PosterPrinterBlockEntity::class.java)

    const val maxInk = 100000

    val downSideSlots = intArrayOf(OUTPUT_SLOT, INK_SLOT) // allow extracting output prints and empty ink cartridges
    val otherSideSlots = intArrayOf(PAPER_SLOT, INK_SLOT)

    val inkValue: Int = config.get("printer.ink_value")

    fun onTick(world: World, pos: BlockPos, state: BlockState, be: PosterPrinterBlockEntity) {
      be.onTick(world)
    }

    fun onClientTick(world: World, pos: BlockPos, state: BlockState, be: PosterPrinterBlockEntity) {
      if (be.cachedState.get(PosterPrinterBlock.printing)) be.animationTicks++
    }
  }
}
