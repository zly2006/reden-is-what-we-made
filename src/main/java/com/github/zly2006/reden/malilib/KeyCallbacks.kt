package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.network.Rollback
import com.github.zly2006.reden.network.TagBlockPos
import com.github.zly2006.reden.sendMessage
import com.github.zly2006.reden.toBlockPos
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode

fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.keybind.setCallback { _, _ ->
        mc.setScreen(configScreen())
        true
    }
    ROLLBACK_KEY.keybind.setCallback { _, _ ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Rollback(0))
            true
        } else false
    }
    REDO_KEY.keybind.setCallback { _, _ ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Rollback(1))
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.keybind.setCallback { _, _ ->
        val pos = mc.crosshairTarget?.pos?.toBlockPos()
        if (pos != null) {
            val new = TagBlockPos.tags.compute(pos.asLong()) { _, old ->
                when (old) {
                    3 -> 0
                    null -> 1
                    else -> old + 1
                }
            }
            mc.player?.sendMessage("OK $pos=$new")
            true
        } else false
    }
    DEBUG_PREVIEW_UNDO.keybind.setCallback { _, _ ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            TagBlockPos.tags.clear()
            val view = mc.server!!.playerManager.playerList[0].data()
            view.undo.lastOrNull()?.keys?.forEach {
                TagBlockPos.tags[it] = 1
            }
            return@setCallback true
        }
        return@setCallback false
    }
}