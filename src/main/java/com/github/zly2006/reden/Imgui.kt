package com.github.zly2006.reden

import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw

// 创建 ImGui 的实例
private val imGuiGlfw = ImGuiImplGlfw()
private val imGuiGl3 = ImGuiImplGl3()
fun initImgui(windowHandle: Long) {
    // 设置 ImGui 的上下文
    ImGui.createContext()

    // 设置 ImGui 的配置
    val io = ImGui.getIO()
    io.configFlags = io.configFlags or ImGuiConfigFlags.NavEnableKeyboard

    // 初始化 ImGui 的 GLFW 和 OpenGL 实现
    imGuiGlfw.init(windowHandle, true)
    imGuiGl3.init("#version 150")
}

fun renderFrame() {
    // 开始新的 ImGui 帧
    imGuiGlfw.newFrame()
    ImGui.newFrame()

    // 在这里添加你的 ImGui 代码
    // 例如，创建一个简单的窗口
    ImGui.begin("Hello, world!")
    ImGui.text("This is some useful text.")
    ImGui.end()

    // 渲染 ImGui 帧
    ImGui.render()
    imGuiGl3.renderDrawData(ImGui.getDrawData())
}
