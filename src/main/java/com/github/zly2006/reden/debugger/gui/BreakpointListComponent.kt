package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.REMOVE
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
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
    val breakpoints: Collection<BreakPoint>
) : FlowLayout(Sizing.content(), Sizing.content(), Algorithm.VERTICAL) {
    val grid = Containers.grid(Sizing.content(), Sizing.content(), breakpoints.size + 10, 7).apply {
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 0)
        child(Components.label(Text.literal("Name")).margins(Insets.horizontal(4)), 0, 1)
        child(Components.label(Text.literal("Enabled")).margins(Insets.horizontal(4)), 0, 2)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 3)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 4)

        child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            child(Components.button(Text.literal("Add breakpoint")) {
                child(Components.dropdown(Sizing.content()).apply {
                    mouseEnter().subscribe { closeWhenNotHovered(true) }
                    val manager = BreakpointsManager.getBreakpointManager()

                    text(Text.literal("Select breakpoint type:"))
                    manager.registry.values.forEach { type ->
                        button(type.description) {
                            val id = (manager.breakpointMap.keys.maxOrNull() ?: 0) + 1
                            val mc = MinecraftClient.getInstance()
                            manager.breakpointMap[id] = type.create(id).apply {
                                world = mc.world!!.registryKey.value
                                setPosition(mc.player!!.blockPos)
                                handler.add(BreakPoint.Handler(FreezeGame(), name = "Behavior 1"))
                            }
                            mc.data.breakpoints.sync(manager.breakpointMap[id])
                        }
                    }
                })
            })
        }, 0, 5)


        breakpoints.mapIndexed { index, breakpoint ->
            val row = index + 1
            child(Components.smallCheckbox(Text.empty()), row, 0)
            child(Components.label(Text.literal(breakpoint.name)), row, 1)
            child(
                Components.smallCheckbox(Text.empty())
                    .checked(breakpoint.flags and UpdateBreakpointPacket.ENABLED != 0)
                    .apply {
                        onChanged().subscribe {
                            val mc = MinecraftClient.getInstance()
                            val flag = if (it) {
                                breakpoint.flags or UpdateBreakpointPacket.ENABLED
                            } else {
                                breakpoint.flags and UpdateBreakpointPacket.ENABLED.inv()
                            }
                            breakpoint.flags = flag
                            mc.data.breakpoints.sync(breakpoint)
                        }
                    }, row, 2
            )
            child(Components.button(Text.literal("Configure")) {
                val mc = MinecraftClient.getInstance()
                mc.setScreen(BreakpointInfoScreen(breakpoint))
            }, row, 3)
            child(Components.button(Text.literal("Delete").red()) {
                ClientPlayNetworking.send(UpdateBreakpointPacket(null, REMOVE, bpId = breakpoint.id))
            }, row, 4)
        }
    }
    init {
        child(grid)
    }

    class Screen(val breakpoints: Collection<BreakPoint>) : BaseOwoScreen<ScrollContainer<BreakpointListComponent>>(),
        BreakpointUpdatable {
        override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
            Containers.verticalScroll(horizontal, vertical, BreakpointListComponent(breakpoints)).apply {
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
