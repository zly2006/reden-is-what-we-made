package com.github.zly2006.reden.access

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class PlayerData(
    player: ServerPlayerEntity,
) {
    val undo: MutableList<UndoRecord> = mutableListOf()
    val redo: MutableList<UndoRecord> = mutableListOf()
    var isRecording: Boolean = false
    var pearlListening: Boolean = false

    fun stopRecording(world: World) {
        isRecording = false
        redo.clear()
        if (undo.lastOrNull() != null) {
            if (undo.last().data.isEmpty()) {
                UpdateMonitorHelper.removeRecord(undo.last().id)
                undo.removeLast()
            }
        }
    }

    data class Entry(
        val blockState: NbtCompound,
        val blockEntity: NbtCompound?
    ) {
        companion object {
            fun fromWorld(world: World, pos: BlockPos): Entry {
                return Entry(
                    NbtHelper.fromBlockState(world.getBlockState(pos)),
                    world.getBlockEntity(pos)?.createNbt()
                )
            }
        }
    }
    internal interface PlayerDataAccess {
        fun getRedenPlayerData(): PlayerData
    }

    companion object {
        fun ServerPlayerEntity.data(): PlayerData {
            return (this as PlayerDataAccess).getRedenPlayerData()
        }
    }

    class UndoRecord(
        val id: Long,
        val data: MutableMap<Long, Entry>
    )
}
