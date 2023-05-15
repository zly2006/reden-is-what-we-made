package com.github.zly2006.reden.access

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import java.util.*

interface PlayerPatchesView {
    data class Entry(
        val blockState: BlockState,
        val blockEntity: NbtCompound?
    )
    val blocks: Queue<MutableMap<BlockPos, Entry>>
}
