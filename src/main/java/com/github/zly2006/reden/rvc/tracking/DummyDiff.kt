package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.nbt.NbtDiff
import net.minecraft.nbt.NbtCompound
import java.util.function.Supplier

object DummyDiff: NbtDiff {
    override fun apply(nbt: Supplier<NbtCompound>): NbtCompound = nbt.get()
    override fun combine(parent: NbtDiff): NbtDiff = parent
}