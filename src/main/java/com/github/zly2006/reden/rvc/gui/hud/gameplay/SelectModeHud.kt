package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import com.github.zly2006.reden.rvc.tracking.tracker.TrackPredicate
import com.github.zly2006.reden.utils.holdingToolItem
import com.github.zly2006.reden.utils.red
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

fun registerHud() {
    RvcHudRenderer.supplierMap["select_mode_hud"] = {
        val list = mutableListOf<Text>()
        val mc = MinecraftClient.getInstance()
        if (mc.player.holdingToolItem) {
            if (selectedStructure == null) {
                list.add(Text.translatable("reden.widget.rvc.hud.selected_nothing"))
            }
            else {
                list.add(Text.translatable("reden.widget.rvc.hud.selected", selectedStructure!!.name))
                val allTrackpoint = selectedStructure!!.regions.values.map { it.tracker }
                    .filterIsInstance<StructureTracker.Trackpoint>()
                    .flatMap { it.trackpoints }
                val trackCount = allTrackpoint.count { it.mode == TrackPredicate.TrackMode.TRACK }
                val ignoreCount = allTrackpoint.count { it.mode == TrackPredicate.TrackMode.IGNORE }
                list.add(Text.translatable("reden.widget.rvc.hud.trackpoints", trackCount, ignoreCount))
                if (selectedStructure!!.placementInfo == null) {
                    list.add(Text.literal("[Warning] Not placed").red())
                }
            }
        }
        list
    }
}
