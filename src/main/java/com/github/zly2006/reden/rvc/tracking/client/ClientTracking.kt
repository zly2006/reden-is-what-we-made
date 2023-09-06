package com.github.zly2006.reden.rvc.tracking.client

import com.github.zly2006.reden.malilib.SELECTION_TOOL
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW

var trackedStructure: TrackedStructure? = null

fun registerSelectionTool() {
    // fixme: DEBUG
    ClientPlayConnectionEvents.JOIN.register{ _, _, mc ->
        trackedStructure = TrackedStructure("test")
        trackedStructure!!.world = mc.world!!
    }

    InputEventHandler.getInputManager().registerMouseInputHandler(object : IMouseInputHandler {
        override fun onMouseClick(mouseX: Int, mouseY: Int, eventButton: Int, eventButtonState: Boolean): Boolean {
            if (!eventButtonState) return false
            val mc = MinecraftClient.getInstance()
            if (mc.currentScreen != null) return false
            val stack = mc.player?.getStackInHand(Hand.MAIN_HAND) ?: return false
            if (Registries.ITEM.getId(stack.item) == Identifier.tryParse(SELECTION_TOOL.stringValue)) {
                // get clicked block
                val raycast = mc.cameraEntity!!.raycast(256.0, 0f, false)
                if (raycast.type == HitResult.Type.BLOCK) {
                    val blockResult = raycast as BlockHitResult
                    mc.player!!.sendMessage(
                        Text.literal("Clicked block: ").append(Text.translatable(mc.world!!.getBlockState(blockResult.blockPos).block.translationKey))
                    )
                    // fixme: DEBUG
                    if (eventButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        trackedStructure?.trackPoints?.add(TrackedStructure.TrackPoint(blockResult.blockPos, TrackedStructure.TrackPredicate.QC, TrackedStructure.TrackPoint.TrackMode.TRACK))
                    }
                    else {
                        trackedStructure?.trackPoints?.add(TrackedStructure.TrackPoint(blockResult.blockPos, TrackedStructure.TrackPredicate.SAME, TrackedStructure.TrackPoint.TrackMode.IGNORE))
                    }
                    trackedStructure?.refreshPositions()
                    trackedStructure?.debugRender()
                } else {
                    mc.player!!.sendMessage(Text.literal("Clicked nothing"))
                }
                return true
            }
            return false
        }
    })
}