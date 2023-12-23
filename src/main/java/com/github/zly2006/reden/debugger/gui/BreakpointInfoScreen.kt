package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.utils.plus
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.sendMessage
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
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
    private var previousName: String = breakpoint.name
    val renameButton = Components.button(Text.literal("Rename")) {
        if (nameInput.text != previousName) {
            breakpoint.name = nameInput.text
            previousName = nameInput.text
            BreakpointsManager.getBreakpointManager().sync(breakpoint)
        }
    }.apply {
        verticalSizing(Sizing.fixed(16))
        active(false)!!
    }
    val nameInput: TextBoxComponent = object: TextBoxComponent(Sizing.fixed(100)) {
        init {
            margins(fuckMojangMargins)
            setDrawsBackground(false)
            text(breakpoint.name)
            onChanged().subscribe {
                if (it == previousName) {
                    renameButton.active(false)
                } else {
                    renameButton.active(true)
                }
            }
        }

        override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            super.renderWidget(context, mouseX, mouseY, delta)
        }
    }
    val infoMetric = object: GridLayout(Sizing.content(), Sizing.content(), 3, 2){
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
    override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
        Containers.verticalScroll(horizontal, vertical, Containers.verticalFlow(horizontal, vertical))
    }!!

    override fun build(p0: ScrollContainer<FlowLayout>) {
        val root = p0.child()
        root.gap(10)
        root.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20)).apply {
            verticalAlignment(VerticalAlignment.CENTER)
            child(Components.label(Text.literal("Name")))
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(nameInput)
                child(renameButton)
                verticalAlignment(VerticalAlignment.CENTER)
                surface(Surface.flat(0x80000000.toInt()) + Surface.outline(0x80FFFFFF.toInt()))
            })
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
                                client!!.player!!.sendMessage(behavior.id.toString())
                            }
                        }
                    })
                    true
                }
            })
            child(Components.button(Text.literal("Remove").red()) {

            }.active(false))
        })
        root.child(Containers.verticalScroll(Sizing.fill(), Sizing.fill(40), BreakpointBehaviorListComponent(breakpoint.handler)).apply {

        })
    }

    class BreakpointBehaviorListComponent(
        val behaviors: List<BreakPoint.Handler>
    ): GridLayout(Sizing.content(), Sizing.content(), behaviors.size + 1, 5) {
        init {
            verticalAlignment(VerticalAlignment.CENTER)

            child(Components.label(Text.literal("Type")), 0, 0)
            child(Components.label(Text.literal("Name")), 0, 1)
            child(Components.label(Text.literal("Priority")), 0, 2)
            child(Components.label(Text.literal("Options")), 0, 3)
            child(Components.label(Text.literal("Delete").red()), 0, 4)

            behaviors.forEachIndexed { index, handler ->
                val row = index + 1
                // todo: handler name i18n
                child(Components.label(Text.literal(handler.type.id.toString())), row, 0)
                child(Components.textBox(Sizing.fill(30), handler.name).apply {
                    setDrawsBackground(false)
                    margins(fuckMojangMargins)
                }.wrap().apply {
                    surface(Surface.flat(0x80000000.toInt()) + Surface.outline(0x80FFFFFF.toInt()))
                }, row, 1)
                child(Components.textBox(Sizing.fill(10), handler.priority.toString()).apply {
                    setDrawsBackground(false)
                    margins(fuckMojangMargins)
                    onChanged().subscribe {
                        handler.priority = it.toInt()
                    }
                }.wrap().apply {
                    surface(Surface.flat(0x80000000.toInt()) + Surface.outline(0x80FFFFFF.toInt()))
                }, row, 2)
            }
        }
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
private val fuckMojangMargins = Insets.of(2, 1, 2, 2)
