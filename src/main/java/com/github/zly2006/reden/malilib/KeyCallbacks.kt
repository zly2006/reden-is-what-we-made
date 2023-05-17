package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.network.Rollback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode


fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.keybind.setCallback { action, bind ->
        mc.setScreen(configScreen())
        true
    }
    TOGGLE_NC_BREAKPOINT.keybind.setCallback { action, bind ->
        true
    }
    ROLLBACK_KEY.keybind.setCallback { action, bind ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Rollback())
            true
        } else false
    }
}