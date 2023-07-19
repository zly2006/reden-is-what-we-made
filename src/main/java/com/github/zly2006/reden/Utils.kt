package com.github.zly2006.reden

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

lateinit var server: MinecraftServer

fun Vec3d.toBlockPos(): BlockPos {
    return BlockPos.ofFloored(this)
}

fun PlayerEntity.sendMessage(s: String) {
    sendMessage(Text.literal(s))
}

fun <E> MutableList<E>.removeAtOrNull(index: Int): E? {
    val i = if (index < 0) size + index else index
    return if (i in indices) removeAt(i) else null
}

fun World.setBlockNoPP(pos: BlockPos, state: BlockState, flags: Int) {
    getChunk(pos).run {
        getSection(getSectionIndex(pos.y))
    }.setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state, false)
    if (this is ServerWorld) {
        chunkManager.markForUpdate(pos)
    }
    if (flags and Block.NOTIFY_LISTENERS != 0) {
        updateListeners(pos, getBlockState(pos), state, flags)
    }
}

val isClient: Boolean get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT
