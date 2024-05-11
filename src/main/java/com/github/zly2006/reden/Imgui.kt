package com.github.zly2006.reden

import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImBoolean
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

// 创建 ImGui 的实例
private val imGuiGlfw = ImGuiImplGlfw()
private val imGuiGl3 = ImGuiImplGl3()
fun initImgui(windowHandle: Long) {
    // 设置 ImGui 的上下文
    ImGui.createContext()

    // 设置 ImGui 的配置
    val io = ImGui.getIO()
    io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
    io.addConfigFlags(ImGuiConfigFlags.DockingEnable)

    // 初始化 ImGui 的 GLFW 和 OpenGL 实现
    imGuiGlfw.init(windowHandle, true)
    imGuiGl3.init("#version 150")
}

val renderers = mutableMapOf<String, () -> Unit>()
val hudRenderers = mutableMapOf<String, () -> Unit>()

open class ImguiScreen(
    var mainRenderer: () -> Unit = {},
) : Screen(Text.literal("reden")) {
    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close()
            return true
        }
        return false
    }

    override fun close() {
        super.close()
        renderers.clear()
    }
}

fun renderFrame() {
    val mc = MinecraftClient.getInstance()
    // 开始新的 ImGui 帧
    imGuiGlfw.newFrame()
    ImGui.newFrame()

    // 在这里添加你的 ImGui 代码
    // 例如，创建一个简单的窗口
    hudRenderers.forEach { (title, renderer) ->
        ImGui.begin(title)
        renderer()
        ImGui.end()
    }
    if (mc.currentScreen is ImguiScreen) {
        run {
            var windowFlags = ImGuiWindowFlags.MenuBar or ImGuiWindowFlags.NoDocking

            val viewport = ImGui.getMainViewport()
            ImGui.setNextWindowPos(viewport.posX, viewport.posY)
            ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
            ImGui.setNextWindowViewport(viewport.id)
            ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f)
            ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f)
            windowFlags =
                windowFlags or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove
            windowFlags = windowFlags or ImGuiWindowFlags.NoBringToFrontOnFocus or ImGuiWindowFlags.NoNavFocus
            windowFlags = windowFlags or ImGuiWindowFlags.NoBackground

            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f)
            ImGui.begin("DockSpace", ImBoolean(true), windowFlags)
            ImGui.popStyleVar()
            ImGui.popStyleVar(2)

            (mc.currentScreen as ImguiScreen).mainRenderer()

            ImGui.end()
        }

        renderers.forEach { (title, renderer) ->
            ImGui.begin(title)
            renderer()
            ImGui.end()
        }
    }

    // 渲染 ImGui 帧
    ImGui.render()
    imGuiGl3.renderDrawData(ImGui.getDrawData())
}
