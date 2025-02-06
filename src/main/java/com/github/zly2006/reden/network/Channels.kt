package com.github.zly2006.reden.network

fun registerChannels() {
    registerHello()
    TagBlockPos.register()
    Undo.register()
    GlobalStatus.register()
    WorldStatus.register()
    HopperCDSync.register()
}
