package com.github.zly2006.reden.gui

import com.github.zly2006.reden.malilib.HiddenOption.data_IDENTIFICATION
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
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class VotingScreen(
    val voting: Voting
): BaseOwoScreen<FlowLayout>() {
    @Serializable
    data class Voting(
        val id: Int = -1,
        val requireEarlyAccess: Boolean = false,
        val name: TranslatableContent,
        val choice: List<Choice>,
        val maxChoose: Int = 1,
        val minChoose: Int = 1,
    ) {
        @Serializable
        class Choice(
            val desc: TranslatableContent,
            val url: String,
            val name: String
        )
    }

    @Serializable
    class TranslatableContent(
        val fallback: String?,
        val key: String?,
        val lang: Map<String, String>
    ) {
        fun get(): MutableText {
            val lang = MinecraftClient.getInstance().languageManager.language
            return Text.literal(this.lang[lang] ?: fallback ?: key ?: "???")
        }
    }

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
