package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.gui.componments.UpdatableTextBox
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text

/**
 * Show player a specified breakpoint's information.
 *
 * Some functions like:
 * + Rename breakpoint
 * + Delete breakpoint
 * + Change behavior (by displaying all breakpoints ordered by priority)
 *  -- Add, remove, update, etc.
 * + Show info: position, world, and everything should be visible to players.
 */
class BreakpointInfoScreen(
    val breakpoint: BreakPoint
): BaseOwoScreen<ScrollContainer<FlowLayout>>() {
    private val nameInput = UpdatableTextBox(Sizing.fixed(100), 16, breakpoint.name) { _, new ->
        breakpoint.name = new
        client!!.data().breakpoints.sync(breakpoint)
        true
    }
    private val infoMetric = object : GridLayout(Sizing.content(), Sizing.content(), 3, 2) {
        init {
            child(Components.label(Text.literal("Type: ")), 0, 0)
            child(Components.label(breakpoint.type.description), 0, 1)

            child(Components.label(Text.literal("Position: ")), 1, 0)
            child(Components.label(Text.literal(breakpoint.pos?.toShortString())), 1, 1)

            child(Components.label(Text.literal("World: ")), 2, 0)
            child(Components.label(Text.literal(breakpoint.world?.toString())), 2, 1)
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            context.fill(
                x,
                y,
                x + determineHorizontalContentSize(Sizing.content()),
                y + determineVerticalContentSize(Sizing.fill(100)),
                0x80000000.toInt()
            )
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }
    private var behaviorListComponent = BreakpointBehaviorListComponent(breakpoint.handler)
    private val removeBehaviorButton = Components.button(Text.literal("Remove").red()) {
        val indexes = behaviorListComponent.selectedIndexes
        indexes.sortBy { -it } // make sure we remove from the end
        indexes.forEach(breakpoint.handler::removeAt)
        indexes.clear()
        refreshBehaviorList()
    }.active(false)
    private lateinit var root: FlowLayout
    override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
        Containers.verticalScroll(horizontal, vertical, Containers.verticalFlow(horizontal, vertical))
    }!!

    override fun build(p0: ScrollContainer<FlowLayout>) {
        root = p0.child()
        root.gap(5)
        root.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
            verticalAlignment(VerticalAlignment.CENTER)
            child(Components.label(Text.literal("Name")))
            child(nameInput)
        })
        root.child(infoMetric)
        root.child(Components.label(Text.literal("Behaviors")).apply {
            mouseEnter().subscribe {
            }
        })
        root.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
            gap(5)
            child(Components.button(Text.literal("Add")) { }.apply {
                mouseDown().subscribe { _, _, _ ->
                    root.child(Components.dropdown(Sizing.content()).also { dropdown ->
                        dropdown.positioning(Positioning.absolute(x + width, y))
                        dropdown.mouseEnter().subscribe { dropdown.closeWhenNotHovered(true) }

                        dropdown.text(Text.literal("Select behavior"))
                        for (behavior in client!!.data().breakpoints.behaviorRegistry.values) {
                            dropdown.button(Text.literal(behavior.id.toString())) {
                                breakpoint.handler.add(BreakPoint.Handler(behavior, name = "New Handler"))
                                client!!.data().breakpoints.sync(breakpoint)
                                refreshBehaviorList()
                            }
                        }
                    })
                    true
                }
            })
            child(removeBehaviorButton)
        })
        root.child(behaviorListComponent)
    }

    inner class BreakpointBehaviorListComponent(behaviors: List<BreakPoint.Handler>) : GridLayout(
        Sizing.content(), Sizing.content(), behaviors.size + 1, 5
    ) {
        val selectedIndexes = mutableListOf<Int>()
        init {
            verticalAlignment(VerticalAlignment.CENTER)

            child(Components.label(Text.literal("Type")), 0, 1)
            child(Components.label(Text.literal("Name")), 0, 2)
            child(Components.label(Text.literal("Priority")), 0, 3)
            child(Components.label(Text.literal("Options")), 0, 4)

            behaviors.forEachIndexed { index, handler ->
                val row = index + 1
                // todo: handler name i18n
                child(Components.smallCheckbox(Text.empty()).apply {
                    onChanged().subscribe {
                        if (it) selectedIndexes.add(index)
                        else selectedIndexes.remove(index)
                        removeBehaviorButton.active(selectedIndexes.isNotEmpty())
                    }
                }, row, 0)
                child(Components.label(Text.literal(handler.type.id.toString())), row, 1)
                child(UpdatableTextBox(Sizing.fixed(100), 12, handler.name) { _, new ->
                    handler.name = new
                    client!!.data().breakpoints.sync(breakpoint)
                    true
                }, row, 2)
                child(UpdatableTextBox(Sizing.fixed(100), 12, handler.priority.toString()) { _, new ->
                    new.toIntOrNull()?.let {
                        handler.priority = it
                        client!!.data().breakpoints.sync(breakpoint)
                        refreshBehaviorList()
                        true
                    } ?: false
                }, row, 3)
            }
        }
    }

    private fun refreshBehaviorList() {
        root.removeChild(behaviorListComponent)
        breakpoint.handler.sortBy { it.priority }
        behaviorListComponent = BreakpointBehaviorListComponent(breakpoint.handler)
        root.child(behaviorListComponent)
    }
}

private fun Component.wrap(): FlowLayout {
    val flow = Containers.horizontalFlow(Sizing.content(), Sizing.content())
    flow.child(this)
    return flow
}

/**
 * To keep our text in the center of the textbox.
 * Don't ask me why top is greater than bottom for 1px, ask Mojang.
 */
val fuckMojangMargins = Insets.of(2, 1, 2, 2)
