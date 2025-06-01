package com.github.zly2006.reden.access

@Suppress("INAPPLICABLE_JVM_NAME")
interface BlockEntityInterface {
    @get:JvmName("getLastSavedNbt\$reden")
    val lastSavedNbt: Any?

    @JvmName("saveLastNbt\$reden")
    fun saveLastNbt()
}
