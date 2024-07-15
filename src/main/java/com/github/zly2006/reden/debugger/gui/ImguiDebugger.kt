package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.access.ServerData
import imgui.ImGui

fun renderDebuggerGui(data: ServerData) {
    if (data.frozen) {
        ImGui.textColored(0xFF7F7F7F.toInt(), "Game was frozen")
    }
    if (ImGui.collapsingHeader("overworld")) {
    }
}
