package com.github.zly2006.reden.access

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

interface PlayerPatchesView {
    data class Entry(
        val blockState: NbtCompound,
        val blockEntity: NbtCompound?
    )
    val blocks: MutableList<MutableMap<BlockPos, Entry>>
    var isRecording: Boolean
}

interface ChainedUpdaterView {
}
