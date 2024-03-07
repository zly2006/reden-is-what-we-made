package com.github.zly2006.reden.rvc.gui.git

import com.github.zly2006.reden.rvc.gui.SelectionInfoScreen
import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.text.Text

class RvcCommitScreen(val repository: RvcRepository, val structure: TrackedStructure) : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    val refreshButton = Components.button(Text.empty()) {}
    val rollbackButton = Components.button(Text.empty()) {}

    val commitMessage = Components.textArea(Sizing.fill(), Sizing.fill(70))

    val commitButton = Components.button(Text.literal("Commit")) {
        val message = commitMessage.text
        if (message.isBlank()) {
            UIErrorToast.report("Commit message cannot be empty")
            return@button
        }

        repository.commit(structure, message, client!!.player)
        client!!.setScreen(SelectionInfoScreen(repository, structure))
    }!!
    val commitAndPushButton = Components.button(Text.literal("Commit and Push")) {
        val message = commitMessage.text
        if (message.isBlank()) {
            UIErrorToast.report("Commit message cannot be empty")
            return@button
        }
        repository.commit(structure, message, client!!.player)
        val remote = object : IRemoteRepository {
            override fun deleteRepo() {
                TODO("Not yet implemented")
            }

            override val gitUrl = repository.git.repository.config.getString("remote", "origin", "url")
        }
        repository.push(remote, false)
        client!!.setScreen(SelectionInfoScreen(repository, structure))
    }!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .gap(5)
            .padding(Insets.of(10))
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent
            .child(Components.label(Text.literal("Commit")))
            .child(Components.label(Text.literal("Message")))
            .child(commitMessage)
            .child(Components.label(Text.literal("Actions")))
            .child(Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(20)).apply {
                gap(5)
                child(commitButton)
                child(commitAndPushButton)
            })
    }

    override fun close() {
        client!!.setScreen(SelectionInfoScreen(repository, structure))
    }
}
