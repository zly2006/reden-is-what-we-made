package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.utils.holdingToolItem
import com.github.zly2006.reden.utils.red
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier


val hudId = Identifier(Reden.MOD_ID, "select_mode_hud")

fun registerHud() {
    RvcHudRenderer.supplierMap["select_mode_hud"] = {
        val list = mutableListOf<Text>()
        val mc = MinecraftClient.getInstance()
        if (mc.player?.holdingToolItem == true) {
            if (selectedStructure == null) {
                list.add(Text.translatable("reden.widget.rvc.hud.selected_nothing"))
            }
            else {
                list.add(Text.translatable("reden.widget.rvc.hud.selected", selectedStructure!!.name))
                val trackCount = selectedStructure!!.trackPoints.count { it.mode == TrackPredicate.TrackMode.TRACK }
                val ignoreCount = selectedStructure!!.trackPoints.count { it.mode == TrackPredicate.TrackMode.IGNORE }
                list.add(
                    Text.translatable("reden.widget.rvc.hud.trackpoints")
                        .append(" ")
                        .append(Text.translatable("reden.widget.rvc.hud.trackpoints.tracking", trackCount).formatted(Formatting.GREEN))
                        .append(" ")
                        .append(Text.translatable("reden.widget.rvc.hud.trackpoints.ignoring", ignoreCount).formatted(Formatting.RED))
                )
                if (selectedStructure!!.placementInfo == null) {
                    list.add(Text.literal("[Warning] Not placed").red())
                }
            }
        }
        list
    }
}
