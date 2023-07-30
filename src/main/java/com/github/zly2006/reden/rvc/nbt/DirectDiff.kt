package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound
import java.util.function.Supplier

class DirectDiff(
    private val after: NbtCompound
): NbtDiff {
    override fun apply(nbt: Supplier<NbtCompound>): NbtCompound {
        return after.copy()
    }

    override fun combine(parent: NbtDiff) = this
}