package com.github.zly2006.reden.rvc.gui.git

import com.github.zly2006.reden.rvc.tracking.RvcRepository
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class RvcManageRemotesScreen(val repository: RvcRepository, val parent: Screen?) : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    val remoteText = Components.textBox(
        Sizing.fill(),
        repository.git.repository.config.getString("remote", "origin", "url")
    ).apply {
        setMaxLength(100)
    }
    val okButton = Components.button(Text.literal("OK")) {
        repository.git.repository.config.setString("remote", "origin", "url", remoteText.text)
        repository.git.repository.config.save()
        close()
    }

    override fun close() {
        client!!.setScreen(parent)
    }

    override fun build(rootComponent: FlowLayout) {
        // todo: multiple remotes, maybe malilib?
        rootComponent
            .gap(5)
            .child(Components.label(Text.literal("Set Remote url:")))
            .child(remoteText)
            .child(okButton)
            .padding(Insets.of(10))
            .surface(Surface.VANILLA_TRANSLUCENT)
    }
}
