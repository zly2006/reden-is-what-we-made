package com.github.zly2006.reden.access

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface PlayerPatchesView {
    fun stopRecording(world: World) {
        isRecording = false
        redo.clear()
        if (undo.lastOrNull() != null) {
            if (undo.last().isEmpty()) {
                undo.removeLast()
            } else {
                redo.add(mutableMapOf<BlockPos, Entry>().apply {
                    undo.last().keys.forEach {
                        this[it] = Entry.fromWorld(world, it)
                    }
                })
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
    val undo: MutableList<MutableMap<BlockPos, Entry>>
    val redo: MutableList<MutableMap<BlockPos, Entry>>
    var isRecording: Boolean
}

interface ChainedUpdaterView {
}
