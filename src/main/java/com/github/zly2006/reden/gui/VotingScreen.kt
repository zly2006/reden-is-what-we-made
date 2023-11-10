package com.github.zly2006.reden.gui

import com.github.zly2006.reden.malilib.data_IDENTIFICATION
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.VerticalAlignment
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

class VotingScreen(

): BaseOwoScreen<FlowLayout>() {
    @Serializable
    class Activity(
        val id: Long,
        val name: String,
        val options: List<String>,
        val url: String,
    )

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        val mc = MinecraftClient.getInstance()
        if (!data_IDENTIFICATION.booleanValue) {
            rootComponent.child(Components.label(Text.literal("Please allow identification data collection in privacy settings to vote and discuss reden features.").red()))
            rootComponent.child(Components.label(Text.literal("We must identify you to prevent abuse. Only online mode Minecraft users can vote.")))
            rootComponent.child(Components.button(Text.literal("Privacy Settings")) {
                mc.setScreen(PrivacyScreen(null))
            })
            rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        }
        else {

        }
    }
}