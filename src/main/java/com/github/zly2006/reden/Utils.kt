package com.github.zly2006.reden

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

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


val isClient: Boolean get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT
