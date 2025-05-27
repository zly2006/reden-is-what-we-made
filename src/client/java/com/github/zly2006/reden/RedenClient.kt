package com.github.zly2006.reden

import com.github.zly2006.reden.malilib.configureKeyCallbacks
import com.github.zly2006.reden.network.registerClientPackets
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft

class RedenClient : ClientModInitializer {
    override fun onInitializeClient() {
        val mc = Minecraft.getInstance()
        configureKeyCallbacks(mc)
        registerClientPackets()
    }
}
