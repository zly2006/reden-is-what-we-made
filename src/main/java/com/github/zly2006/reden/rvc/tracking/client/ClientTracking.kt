package com.github.zly2006.reden.rvc.tracking.client

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.PlacementInfo
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import com.github.zly2006.reden.rvc.tracking.tracker.TrackPoint
import com.github.zly2006.reden.rvc.tracking.tracker.TrackPredicate
import com.github.zly2006.reden.utils.holdingToolItem
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW

fun registerSelectionTool() {
    InputEventHandler.getInputManager().registerMouseInputHandler(object : IMouseInputHandler {
        override fun onMouseClick(mouseX: Int, mouseY: Int, eventButton: Int, eventButtonState: Boolean): Boolean {
            if (!eventButtonState) return false // ensure mouse down
            val mc = MinecraftClient.getInstance()
            if (mc.currentScreen != null) return false // ensure no gui
            if (!mc.player.holdingToolItem) return false // ensure hand tool item

            // get clicked block
            val raycast = mc.cameraEntity!!.raycast(256.0, 0f, false)
            if (raycast.type == HitResult.Type.BLOCK) {
                val blockResult = raycast as BlockHitResult
                if (selectedStructure != null && selectedStructure!!.placementInfo != null) {
                    val structure = selectedStructure!!
                    structure.networkWorker?.launch {
                        val region = structure.regions.values.first() // todo: select region
                        when (val tracker = region.tracker) {
                            is StructureTracker.Trackpoint -> {
                                if (region.placementInfo == null) {
                                    Reden.LOGGER.info(
                                        "PlacementInfo is null for ${structure.name}, creating new one " +
                                                "because of trackpoint creation"
                                    )
                                    region.placementInfo = PlacementInfo(mc.getWorldInfo())
                                }
                                if (eventButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                    tracker.addTrackPoint(
                                        TrackPoint(
                                            region.getRelativeCoordinate(blockResult.blockPos),
                                            TrackPredicate.QC,
                                            TrackPredicate.TrackMode.TRACK
                                        )
                                    )
                                }
                                else {
                                    tracker.addTrackPoint(
                                        TrackPoint(
                                            region.getRelativeCoordinate(blockResult.blockPos),
                                            TrackPredicate.Same,
                                            TrackPredicate.TrackMode.IGNORE,
                                        )
                                    )
                                }
                            }

                            is StructureTracker.Cuboid -> {
                                if (eventButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                    tracker.first = region.getRelativeCoordinate(blockResult.blockPos)
                                    mc.player?.sendMessage(Text.literal("First point set"), true)
                                }
                                else {
                                    tracker.second = region.getRelativeCoordinate(blockResult.blockPos)
                                    mc.player?.sendMessage(Text.literal("Second point set"), true)
                                }
                            }

                            is StructureTracker.Entire -> {}
                            is StructureTracker.Reference -> TODO()
                        }
                        structure.refreshPositions()
                    }
                }
            }
            return true
        }
    })
}

private fun <T> Deferred<T>.ignore(): Job = this
