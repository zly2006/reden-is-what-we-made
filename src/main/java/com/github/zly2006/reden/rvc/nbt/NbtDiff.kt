package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.function.Supplier

interface NbtDiff {
    val type: Int
    fun apply(nbt: Supplier<NbtCompound>): NbtCompound
    fun combine(parent: NbtDiff): NbtDiff
    fun writeBuf(buf: PacketByteBuf)
}