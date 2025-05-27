package com.github.zly2006.reden.utils

import net.minecraft.nbt.NbtCompound

interface DataHolder {
    fun load(): NbtCompound
    fun set(nbt: NbtCompound)
}
