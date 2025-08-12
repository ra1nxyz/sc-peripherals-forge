package io.sc3.library.networking


import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.chunk.LevelChunk

import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ChunkMap

object NetworkUtil {
  fun sendToAllTracking(chunk: LevelChunk, packet: ScLibraryPacket) {
    val level = chunk.level as? ServerLevel ?: return
    val chunkMap = level.chunkSource.chunkMap
    val chunkPos = chunk.pos
    val players = chunkMap.getPlayers(chunkPos, false)
    for (player in players) {
      player.connection.send(packet.toS2CPacket())
    }
  }
}
    