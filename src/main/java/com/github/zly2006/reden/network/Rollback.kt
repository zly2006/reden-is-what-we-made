package com.github.zly2006.reden.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import com.github.zly2006.reden.sendMessage
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

private val pType = PacketType.create(ROLLBACK) {
    Rollback(it.readVarInt())
}
class Rollback(
    val status: Int = 0
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(status)
    }

    companion object {
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                val view = player.data()
                view.isRecording = false
                fun operate(map: MutableMap<Long, PlayerData.Entry>): MutableMap<Long, PlayerData.Entry> {
                    val ret = map.keys.associateWith {
                        PlayerData.Entry.fromWorld(
                            player.world,
                            BlockPos.fromLong(it)
                        )
                    }.toMutableMap()
                    map.forEach { (pos, entry) ->
                        val state = NbtHelper.toBlockState(Registries.BLOCK.readOnlyWrapper, entry.blockState)
                        if (DEBUG_LOGGER.booleanValue) {
                            player.sendMessage("undo ${BlockPos.fromLong(pos)}, $state")
                        }
                        player.world.setBlockNoPP(
                            BlockPos.fromLong(pos),
                            state,
                            Block.NOTIFY_LISTENERS
                        )
                        entry.blockEntity?.let { be ->
                            player.world.getBlockEntity(BlockPos.fromLong(pos))?.readNbt(be)
                        }
                    }
                    return ret
                }
                res.sendPacket(Rollback(when (packet.status) {
                    0 -> view.undo.removeLastOrNull()?.let {
                        view.redo.add(operate(it))
                        0
                    } ?: 2

                    1 -> view.redo.removeLastOrNull()?.let {
                        view.undo.add(operate(it))
                        1
                    } ?: 2

                    else -> 65536
                }))
            }
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                player.sendMessage(
                    when (packet.status) {
                        0 -> Text.literal("Rollback success")
                        1 -> Text.literal("Restore success")
                        2 -> Text.literal("No blocks info")
                        16 -> Text.literal("No permission")
                        32 -> Text.literal("Not recording")
                        65536 -> Text.literal("Unknown error")
                        else -> Text.literal("Unknown status")
                    }
                )
            }
        }
    }
}

private fun World.setBlockNoPP(pos: BlockPos, state: BlockState, flags: Int) {
    infix fun Int.umod(b: Int): Int =
        this % b + if (this < 0) b else 0

    val result = getChunk(pos).run {
        getSection(getSectionIndex(pos.y))
    }.setBlockState(pos.x umod 16, pos.y umod 16, pos.z umod 16, state, false)
    val after = getBlockState(pos)
    if ((flags and Block.SKIP_LIGHTING_UPDATES) == 0 && after != result &&
        (after.getOpacity(this, pos) != result.getOpacity(this, pos) ||
                after.luminance != result.luminance ||
                after.hasSidedTransparency() ||
                result.hasSidedTransparency())
    ) {
        profiler.push("queueCheckLight")
        this.chunkManager.lightingProvider.checkBlock(pos)
        profiler.pop()
    }
    if (this is ServerWorld) {
        chunkManager.markForUpdate(pos)
    }
    if (flags and Block.NOTIFY_LISTENERS != 0) {
        updateListeners(pos, getBlockState(pos), state, flags)
    }
}
