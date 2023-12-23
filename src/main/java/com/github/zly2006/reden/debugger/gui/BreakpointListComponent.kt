package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.REMOVE
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.GridLayout
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
): GridLayout(Sizing.content(), Sizing.content(), breakpoints.size + 1, 5) {
    init {
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 0)
        child(Components.label(Text.literal("Name")).margins(Insets.horizontal(4)), 0, 1)
        child(Components.label(Text.literal("Enabled")).margins(Insets.horizontal(4)), 0, 2)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 3)
        child(Components.label(Text.literal("")).margins(Insets.horizontal(4)), 0, 4)

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

    class Screen(val breakpoints: Collection<BreakPoint>): BaseOwoScreen<ScrollContainer<BreakpointListComponent>>(), BreakpointUpdatable {
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
