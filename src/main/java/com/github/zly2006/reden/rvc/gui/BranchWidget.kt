package com.github.zly2006.reden.rvc.gui

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text
import org.eclipse.jgit.internal.storage.commitgraph.CommitGraph
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk

class BranchWidget(
    repo: Repository,
    latest: ObjectId,
): Selectable, Drawable, Element {
    private val elements: CommitGraph
    init {
        RevWalk(repo).use { walk ->
            val master = walk.parseCommit(latest)
            walk.markStart(master)
            walk.forEach { commit ->
                if (commit.id == latest) return@forEach
                walk.parseCommit(commit)
            }
            elements = walk.objectReader.commitGraph.get()
        }
    }
    internal var scrollAmount = 0.0
    private var selectionType = Selectable.SelectionType.NONE
    override fun isNarratable() = false
    override fun appendNarrations(builder: NarrationMessageBuilder) { }
    override fun getType(): Selectable.SelectionType = selectionType
    override fun setFocused(focused: Boolean) {
        selectionType = if (focused) Selectable.SelectionType.FOCUSED else Selectable.SelectionType.NONE
    }
    override fun isFocused() = selectionType == Selectable.SelectionType.FOCUSED

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        TODO("Not yet implemented")
    }

    class CommitGraphElement(
    ): WWidget() {
        @JvmField var isFocused = false
        override fun paint(context: DrawContext?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            WLabel(Text.of("")).height
            super.paint(context, x, y, mouseX, mouseY)
        }
    }
}