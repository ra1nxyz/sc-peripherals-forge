package io.sc3.library.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Base class for custom Forge packets. Subclasses must implement write and handle methods.
 */
abstract class ScLibraryPacket {
  abstract val id: ResourceLocation

  /** Write this packet's data to the buffer. */
  abstract fun write(buf: FriendlyByteBuf)

  /** Handle this packet on the client. Override in subclasses. */
  open fun handleClient(listener: net.minecraft.network.protocol.game.ClientGamePacketListener) {}

  /**
   * Create a Forge S2C packet for sending to a player.
   * Usage: player.connection.send(packet.toS2CPacket())
   */
  fun toS2CPacket(): Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> = object : Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> {
    override fun write(buf: FriendlyByteBuf) {
      this@ScLibraryPacket.write(buf)
    }

    override fun handle(listener: net.minecraft.network.protocol.game.ClientGamePacketListener) {
      this@ScLibraryPacket.handleClient(listener)
    }
  }
}