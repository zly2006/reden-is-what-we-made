package com.github.zly2006.reden

import com.github.zly2006.reden.gui.FontAwesomeIcons
import com.github.zly2006.reden.utils.ResourceLoader
import imgui.ImFontConfig
import imgui.ImFontGlyphRangesBuilder
import imgui.ImGui
import imgui.ImGuiIO
import imgui.assertion.ImAssertCallback
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

class ImGuiAssertationException(assertion: String?, line: Int, file: String?) :
    RuntimeException("ImGui assertion failed: $assertion at $file:$line")

fun initImgui(windowHandle: Long) {
    // 设置 ImGui 的上下文
    ImGui.createContext()

    // 设置 ImGui 的配置
    val io = ImGui.getIO()
    io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
    io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
    initFonts(io)

    // 初始化 ImGui 的 GLFW 和 OpenGL 实现
    imGuiGlfw.init(windowHandle, true)
    imGuiGl3.init("#version 150")

    ImGui.setAssertCallback(object : ImAssertCallback() {
        override fun imAssert(assertion: String?, line: Int, file: String?) {
            throw ImGuiAssertationException(assertion, line, file)
        }

        override fun imAssertCallback(p0: String?, p1: Int, p2: String?) {
        }
    })
}

private fun initFonts(io: ImGuiIO) {
    io.fonts.addFontDefault() // Add default font for latin glyphs

    // You can use the ImFontGlyphRangesBuilder helper to create glyph ranges based on text input.
    // For example: for a game where your script is known, if you can feed your entire script to it (using addText) and only build the characters the game needs.
    // Here we are using it just to combine all required glyphs in one place
    val rangesBuilder = ImFontGlyphRangesBuilder() // Glyphs ranges provide
    rangesBuilder.addRanges(io.fonts.glyphRangesDefault)
    rangesBuilder.addRanges(io.fonts.glyphRangesCyrillic)
    rangesBuilder.addRanges(io.fonts.glyphRangesJapanese)
    rangesBuilder.addRanges(io.fonts.glyphRangesChineseFull)
    rangesBuilder.addRanges(io.fonts.glyphRangesChineseSimplifiedCommon)
    rangesBuilder.addRanges(io.fonts.glyphRangesKorean)
    rangesBuilder.addRanges(FontAwesomeIcons._IconRange)
    rangesBuilder.addRanges(shortArrayOf(0, 0xffff.toShort()))

    // Font config for additional fonts
    // This is a natively allocated struct so don't forget to call destroy after atlas is built
    val fontConfig = ImFontConfig()
    fontConfig.mergeMode = true // Enable merge mode to merge cyrillic, japanese and icons with default font

    val glyphRanges = rangesBuilder.buildRanges()
    io.fonts.addFontFromMemoryTTF(
        ResourceLoader.loadBytes("Tahoma.ttf"),
        14f,
        fontConfig,
        glyphRanges
    ) // cyrillic glyphs
    io.fonts.addFontFromMemoryTTF(
        ResourceLoader.loadBytes("NotoSansCJKjp-Medium.otf"),
        16f,
        fontConfig,
        glyphRanges
    ) // japanese glyphs
    io.fonts.addFontFromMemoryTTF(
        ResourceLoader.loadBytes("fa-regular-400.ttf"),
        14f,
        fontConfig,
        glyphRanges
    ) // font awesome
    io.fonts.addFontFromMemoryTTF(
        ResourceLoader.loadBytes("fa-solid-900.ttf"),
        14f,
        fontConfig,
        glyphRanges
    ) // font awesome
    io.fonts.build()

    fontConfig.destroy()
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
