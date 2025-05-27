package com.github.zly2006.reden.access

import net.minecraft.nbt.CompoundTag

@Suppress("INAPPLICABLE_JVM_NAME")
interface BlockEntityInterface {
    @get:JvmName("getLastSavedNbt\$reden")
    val lastSavedNbt: CompoundTag?

    @JvmName("saveLastNbt\$reden")
    fun saveLastNbt()
}
