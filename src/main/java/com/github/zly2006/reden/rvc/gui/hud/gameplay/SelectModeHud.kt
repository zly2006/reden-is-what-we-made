package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.handToolItem
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
        if (mc.player?.handToolItem == true) {
            if (selectedStructure == null) {
                list.add(Text.literal("Selected Nothing.").red())
            }
            else {
                list.add(Text.translatable("Selecting: ", selectedStructure!!.name))
                val trackCount = selectedStructure!!.trackPoints.count { it.mode == TrackedStructure.TrackPoint.TrackMode.TRACK }
                val ignoreCount = selectedStructure!!.trackPoints.count { it.mode == TrackedStructure.TrackPoint.TrackMode.IGNORE }
                list.add(
                    Text.literal("Trackpoints: ")
                        .append(Text.literal("$trackCount Tracking").formatted(Formatting.GREEN))
                        .append(Text.literal("$ignoreCount Ignoring").formatted(Formatting.RED))
                )
            }
        }
        list
    }
}
