package com.github.zly2006.reden.access

import net.minecraft.nbt.NbtCompound

@Suppress("INAPPLICABLE_JVM_NAME")
interface BlockEntityInterface {
    @get:JvmName("getLastSavedNbt\$reden")
    val lastSavedNbt: NbtCompound?

    @JvmName("saveLastNbt\$reden")
    fun saveLastNbt()
}
