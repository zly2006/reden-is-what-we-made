package com.github.zly2006.reden.debugger.storage

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World

class BlocksResetStorage: ResetStorage {
    val beforeChanged = Long2ObjectOpenHashMap<BlockState>()
    val blockEntities = Long2ObjectOpenHashMap<NbtCompound>()

    override fun clear() {
        beforeChanged.clear()
        blockEntities.clear()
    }

    override fun apply(world: World) {

    }
}
