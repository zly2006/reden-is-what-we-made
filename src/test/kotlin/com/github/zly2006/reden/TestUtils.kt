package com.github.zly2006.reden

import net.fabricmc.loader.impl.launch.knot.KnotClient
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.BeforeAll

@BeforeAll
fun setupMinecraftRegistries() {
    SharedConstants.createGameVersion()
    Bootstrap.initialize()
}

fun setupMinecraftClient() {

    KnotClient.main(arrayOf())
}
