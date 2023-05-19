package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.access.PlayerPatchesView
import com.github.zly2006.reden.network.Rollback
import com.github.zly2006.reden.network.TagBlockPos
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode


private fun Vec3d.toBlockPos(): BlockPos {
    return BlockPos.ofFloored(this)
}

private fun PlayerEntity.sendMessage(s: String) {
    sendMessage(Text.literal(s))
}

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
            ClientPlayNetworking.send(Rollback(0))
            true
        } else false
    }
    REDO_KEY.keybind.setCallback { action, bind ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Rollback(1))
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.keybind.setCallback { action, bind ->
        val pos = mc.crosshairTarget?.pos?.toBlockPos()
        if (pos != null) {
            val new = TagBlockPos.tags.compute(pos) { _, old ->
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
    PREVIEW_UNDO.keybind.setCallback { action, bind ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            val view = mc.server!!.playerManager.playerList[0] as PlayerPatchesView
            view.undo.lastOrNull()?.keys?.forEach {
                TagBlockPos.tags[it] = 1
            }
            return@setCallback true
        }
        return@setCallback false
    }
}