package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.function.Supplier

object DummyDiff: NbtDiff {
    override val type: Int = 0
    override fun apply(nbt: Supplier<NbtCompound>): NbtCompound = nbt.get()
    override fun combine(parent: NbtDiff): NbtDiff = parent
    override fun writeBuf(buf: PacketByteBuf) {
        buf.writeByte(0)
    }
}