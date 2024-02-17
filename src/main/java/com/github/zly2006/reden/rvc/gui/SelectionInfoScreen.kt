package com.github.zly2006.reden.rvc.gui

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
    inner class ReversionLine(
        val commit: RevCommit
    ) : FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {

    }

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(Components.label(Text.literal("Name: ${structure.name}")))
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
