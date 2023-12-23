package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.network.UpdateBreakpointPacket

interface BreakpointUpdatable {
    fun updateBreakpoint(packet: UpdateBreakpointPacket)
}