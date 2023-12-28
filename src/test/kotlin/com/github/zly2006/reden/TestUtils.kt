package com.github.zly2006.reden

import net.fabricmc.loader.impl.launch.knot.KnotClient
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants

fun setupMinecraftRegistries() {
    SharedConstants.createGameVersion()
    Bootstrap.initialize()
}

fun setupMinecraftClient() {

    KnotClient.main(arrayOf())
}
