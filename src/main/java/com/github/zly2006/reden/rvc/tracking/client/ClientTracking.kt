package com.github.zly2006.reden.rvc.tracking.client

import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.holdingToolItem
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW

fun registerSelectionTool() {
    InputEventHandler.getInputManager().registerMouseInputHandler(object : IMouseInputHandler {
        override fun onMouseClick(mouseX: Int, mouseY: Int, eventButton: Int, eventButtonState: Boolean): Boolean {
            if (!eventButtonState) return false // ensure mouse down
            val mc = MinecraftClient.getInstance()
            if (mc.currentScreen != null) return false // ensure no gui
            if (mc.player?.holdingToolItem != true) return false // ensure hand tool item

            // get clicked block
            val raycast = mc.cameraEntity!!.raycast(256.0, 0f, false)
            if (raycast.type == HitResult.Type.BLOCK) {
                val blockResult = raycast as BlockHitResult
                if (selectedStructure != null && selectedStructure!!.placementInfo != null) {
                    val structure = selectedStructure!!
                    if (eventButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        structure.addTrackPoint(
                            TrackedStructure.TrackPoint(
                                structure.getRelativeCoordinate(blockResult.blockPos),
                                TrackPredicate.QC,
                                TrackPredicate.TrackMode.TRACK
                            )
                        )
                    }
                    else {
                        structure.addTrackPoint(
                            TrackedStructure.TrackPoint(
                                structure.getRelativeCoordinate(blockResult.blockPos),
                                TrackPredicate.Same,
                                TrackPredicate.TrackMode.IGNORE,
                            )
                        )
                    }
                    structure.networkWorker?.async {
                        structure.refreshPositions()
                    }
                }
            }
            return true
        }
    })
}
