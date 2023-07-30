package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound
import java.util.function.Supplier

interface NbtDiff {
    fun apply(nbt: Supplier<NbtCompound>): NbtCompound
    fun combine(parent: NbtDiff): NbtDiff
}