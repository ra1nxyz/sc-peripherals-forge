package io.sc3.library.networking

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/* r/w methods idk how its handled ill see later */
abstract class ScLibraryPacket {
  abstract val id: ResourceLocation

  abstract fun write(buf: FriendlyByteBuf)

  open fun handleClient(listener: net.minecraft.network.protocol.game.ClientGamePacketListener) {}

  /**
   * player.connection.send(packet.toS2CPacket())
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