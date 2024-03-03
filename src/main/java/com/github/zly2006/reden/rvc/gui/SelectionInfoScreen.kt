package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Edit trackpoints
 * Edit name
 * Edit description
 * Auto-backup (commit)
 */
class SelectionInfoScreen(
    val repository: RvcRepository,
    val structure: TrackedStructure
): BaseOwoScreen<FlowLayout>() {
    private val deleteButton = Components.button(Text.literal("Delete")) {
        // todo confirm screen
        onFunctionUsed("delete_rvcStructure")
        if (selectedRepository == repository) {
            selectedRepository = null
        }
        client!!.data.rvcStructures.remove(repository.name)
        repository.delete()
        client!!.setScreen(SelectionListScreen())
    }!!
    inner class ReversionLine(
        val commit: RevCommit
    ) : FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        val shortHash = Components.label(Text.literal(commit.name.substring(0, 7)))
        val message = Components.label(Text.literal(commit.shortMessage))
        val time = Components.label(Text.literal(commit.commitTime.toString()))
        val author = Components.label(Text.literal(commit.authorIdent.name))
        val cehckoutButton = Components.button(Text.literal("Checkout")) {
            onFunctionUsed("checkout_rvcStructure")
            structure.clearArea()
            repository.checkout(commit.name)
            structure.paste()
            client!!.setScreen(SelectionListScreen())
            // todo
        }
    }

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .gap(5)
            .padding(Insets.of(10))
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(Components.label(Text.literal("Name: ${structure.name}")))
        rootComponent.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
            child(deleteButton)
        })
        rootComponent.child(
            Components.label(
                Text.literal(
                    "Head: ${
                        repository.headHash.substring(
                            0,
                            7
                        )
                    } on ${repository.headBranch}"
                )
            )
        )
    }
}
