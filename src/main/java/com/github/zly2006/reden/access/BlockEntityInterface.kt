package com.github.zly2006.reden.access

import net.minecraft.nbt.NbtCompound

interface BlockEntityInterface {
    fun getLastSavedNbt(): NbtCompound?
    fun saveLastNbt()
}
