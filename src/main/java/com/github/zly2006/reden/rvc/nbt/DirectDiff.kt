package com.github.zly2006.reden.rvc.nbt

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.function.Supplier

class DirectDiff(
    private val after: NbtCompound
): NbtDiff {
    override val type: Int = 1
    override fun apply(nbt: Supplier<NbtCompound>): NbtCompound {
        return after.copy()
    }

    override fun combine(parent: NbtDiff) = this
    override fun writeBuf(buf: PacketByteBuf) {
        buf.writeByte(1)
        buf.writeNbt(after)
    }
}