package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.network.TagBlockPos
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.REMOVE
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

/**
 * Show players a list of breakpoints.
 * It can be all breakpoints, or only breakpoints in a specific world.
 */
class BreakpointListComponent(
    val breakpoints: List<BreakPoint>,
    val title: Text = Text.literal("Breakpoints:"),
) : FlowLayout(Sizing.content(), Sizing.fill(), Algorithm.VERTICAL) {
    private val addButton = Components.button(Text.literal("Add breakpoint")) {
        val mc = MinecraftClient.getInstance()
        onFunctionUsed("buttonAddBreakpoint_listScreen")
        // Note: add child to the root component, so it won't affect the layout of the grid
        this@BreakpointListComponent.child(Components.dropdown(Sizing.content()).apply {
            mouseEnter().subscribe { closeWhenNotHovered(true) }
            positioning(Positioning.absolute(it.x, it.y + it.height))
            val manager = mc.data.breakpoints

            text(Text.literal("Select breakpoint type:"))
            manager.registry.values.forEach { type ->
                button(type.description) {
                    val id = (manager.breakpointMap.keys.maxOrNull() ?: 0) + 1
                    manager.breakpointMap[id] = type.create(id).apply {
                        world = mc.world!!.registryKey.value
                        setPosition(mc.player!!.blockPos)
                        handler.add(BreakPoint.Handler(FreezeGame(), name = "Behavior 1"))
                    }
                    manager.sync(manager.breakpointMap[id])
                }
            }
        })
    }
    private val toggleEnableButton = Components.button(Text.literal("Enable/Disable")) {
        onFunctionUsed("buttonToggleEnableSelectedBreakpoints_listScreen")
        selectedIndexed.map { breakpoints[it] }.forEach {
            it.flags = it.flags xor UpdateBreakpointPacket.ENABLED
            ClientPlayNetworking.send(UpdateBreakpointPacket(null, it.flags, it.id))
            // auto update UI after received server response
        }
    }
    private val deleteButton = Components.button(Text.literal("Delete").red()) {
        onFunctionUsed("buttonDeleteSelectedBreakpoints_listScreen")
        selectedIndexed.map { breakpoints[it] }.forEach {
            ClientPlayNetworking.send(UpdateBreakpointPacket(null, REMOVE, it.id))
        }
        // auto update UI after received server response
    }.active(false)
    private val selectedIndexed = mutableSetOf<Int>()
    private val grid = Containers.grid(Sizing.content(), Sizing.content(), breakpoints.size + 1, 6).apply {
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 0)
        child(Components.label(Text.literal("Name")).margins(Insets.horizontal(4)), 0, 1)
        child(Components.label(Text.literal("Enabled")).margins(Insets.horizontal(4)), 0, 2)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 3)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 4)

        breakpoints.mapIndexed { index, breakpoint ->
            val row = index + 1
            child(object : CheckboxComponent(Text.empty()) {
                override fun draw(
                    context: OwoUIDrawContext?,
                    mouseX: Int,
                    mouseY: Int,
                    partialTicks: Float,
                    delta: Float
                ) {
                    super.draw(context, mouseX, mouseY, partialTicks, delta)
                    if (mouseY in y..y + height) {
                        // Note: we only check y-axis, because this hover is for the whole row
                        if (breakpoint.pos != null) {
                            BlockBorder.tags.clear()
                            BlockBorder[breakpoint.pos!!] = TagBlockPos.green
                        }
                    }
                }

                init {
                    onChanged {
                        if (it) selectedIndexed.add(index)
                        else selectedIndexed.remove(index)
                        deleteButton.active(selectedIndexed.isNotEmpty())
                        toggleEnableButton.active(selectedIndexed.isNotEmpty())
                    }
                }
            }, row, 0)
            child(Components.label(Text.literal(breakpoint.name)), row, 1)
            child(
                Components.checkbox(Text.empty())
                    .checked(breakpoint.flags and UpdateBreakpointPacket.ENABLED != 0)
                    .onChanged {
                        val mc = MinecraftClient.getInstance()
                        val flag = if (it) {
                            breakpoint.flags or UpdateBreakpointPacket.ENABLED
                        } else {
                            breakpoint.flags and UpdateBreakpointPacket.ENABLED.inv()
                        }
                        breakpoint.flags = flag
                        mc.data.breakpoints.sync(breakpoint)
                    }, row, 2
            )
            child(Components.button(Text.literal("Configure")) {
                onFunctionUsed("buttonConfigureBreakpoint_listScreen")
                val mc = MinecraftClient.getInstance()
                mc.setScreen(BreakpointInfoScreen(breakpoint))
            }, row, 3)
            child(Components.button(Text.literal("Delete").red()) {
                onFunctionUsed("buttonDeleteBreakpoint_listScreen")
                ClientPlayNetworking.send(UpdateBreakpointPacket(null, REMOVE, bpId = breakpoint.id))
            }, row, 4)
        }
    }

    init {
        val mc = MinecraftClient.getInstance()
        gap(5)
        child(Components.label(title))
        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            child(addButton)
            child(toggleEnableButton)
            child(deleteButton)
        })
        child(grid)
    }

    class Screen(val breakpoints: Collection<BreakPoint>) : BaseOwoScreen<ScrollContainer<BreakpointListComponent>>(),
        BreakpointUpdatable {
        override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
            Containers.verticalScroll(horizontal, vertical, BreakpointListComponent(breakpoints.toList())).apply {
                surface(Surface.VANILLA_TRANSLUCENT)
            }
        }!!

        override fun build(p0: ScrollContainer<BreakpointListComponent>) {
            // noop
        }

        override fun updateBreakpoint(packet: UpdateBreakpointPacket) {
            client!!.setScreen(Screen(client!!.data.breakpoints.breakpointMap.values))
        }
    }
}
