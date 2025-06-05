package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.network.Undo
import com.github.zly2006.reden.utils.multiver.Text
import com.github.zly2006.reden.utils.red
import fi.dy.masa.malilib.config.options.ConfigHotkey
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft

fun configureKeyCallbacks(mc: Minecraft) {
    REDEN_CONFIG_KEY.callback {
        mc.setScreen(GuiConfigs())
        true
    }
    UNDO_KEY.callback {
        if (mc.gameMode?.playerMode?.isCreative != true)
            return@callback false
        else
            ClientPlayNetworking.send(Undo(0))
        true
    }
    REDO_KEY.callback {
        if (mc.gameMode?.playerMode?.isCreative == true) {
            ClientPlayNetworking.send(Undo(1))
            true
        } else false
    }
}

private fun ConfigHotkey.callback(action: () -> Boolean) {
    keybind.setCallback { _, _ ->
        try {
            if (action()) {
//                onFunctionUsed(name)
                true
            } else false
        } catch (e: Exception) {
            Reden.LOGGER.error("Error when executing hotkey $name", e)
//            reportException(e)
//? if <= 1.21.1 {
            Minecraft.getInstance().player?.sendSystemMessage(Text.literal("Error when executing hotkey $name").red())
//?} else {
            /*Minecraft.getInstance().player?.displayClientMessage(Text.literal("Error when executing hotkey $name").red(), false)
*///?}
            false
        }
    }
}
