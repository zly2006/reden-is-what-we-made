package com.github.zly2006.reden

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants

fun setupMinecraftRegistries() {
    SharedConstants.createGameVersion()
    Bootstrap.initialize()
}
