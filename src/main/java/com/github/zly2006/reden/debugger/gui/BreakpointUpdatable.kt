package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.network.UpdateBreakpointPacket

/**
 * When the client received a [UpdateBreakpointPacket], the screen with this interface
 * is able to update its content to reflect the change.
 */
interface BreakpointUpdatable {
    fun updateBreakpoint(packet: UpdateBreakpointPacket)
}
