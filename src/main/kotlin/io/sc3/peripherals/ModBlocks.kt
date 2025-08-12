package io.sc3.peripherals

import io.sc3.peripherals.block.ChameliumBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object ModBlocks {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, "scperipherals")

    val CHAMELIUM_BLOCK: RegistryObject<Block> = BLOCKS.register("chamelium_block") {
        ChameliumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f))
    }

    fun register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().modEventBus)
    }
}
