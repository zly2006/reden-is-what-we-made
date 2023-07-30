package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound

interface NbtDiff {
    fun apply(nbt: NbtCompound): NbtCompound
    fun combine(another: NbtDiff)
}