package com.github.zly2006.reden.gui.message

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.malilib.OPEN_NOTIFICATIONS_SCREEN
import fi.dy.masa.malilib.util.FileUtils
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object ClientMessageQueue {
    private val temporaryDontShow = mutableSetOf<String>()
    private val dontShowAgain = mutableSetOf<String>()
    private val dontShowAgainConfig = FileUtils.getConfigDirectory().resolve("reden/notifications_dont_show_again.txt")

    init {
        if (dontShowAgainConfig.exists()) {
            dontShowAgainConfig.readLines().forEach {
                dontShowAgain.add(it)
            }
        }
        temporaryDontShow.addAll(dontShowAgain)
    }

    private val toastType = SystemToast.Type()
    class Button(
        val message: Text,
        val action: () -> Unit
    )
    class Message(
        val icon: Identifier,
        val title: Text,
        val body: Text,
        val buttons: List<Button>,
        val textureSize: Int = 16
    ) : FlowLayout(Sizing.fixed(200), Sizing.content(), Algorithm.VERTICAL) {
        override fun mount(parent: ParentComponent?, x: Int, y: Int) {
            super.mount(parent, x, y)
            child(Components.label(title).shadow(true).horizontalSizing(Sizing.fill()))
            child(Components.label(body).horizontalSizing(Sizing.fill()))
            child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                gap(5)
                buttons.forEach {
                    child(Components.label(it.message).apply {
                        mouseDown().subscribe { x, y, b ->
                            it.action()
                            true
                        }
                        mouseEnter().subscribe {
                            text(text().copy().setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true)))
                        }
                        mouseLeave().subscribe {
                            text(
                                text().copy().setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(false))
                            )
                        }
                    })
                }
            })
        }

        override fun dismount(reason: Component.DismountReason?) {
            super.dismount(reason)
            clearChildren()
        }

        init {
            surface(Surface.PANEL)
            verticalAlignment(VerticalAlignment.CENTER)
            this.padding(Insets.of(5, 5, 40, 5))
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)

            context.drawTexture(icon, x + 5, y + 5, 32, 32, 0f, 0f, 16, 16, textureSize, textureSize)
        }
    }

    private var nextId = 0
    val messages: MutableMap<Int, Message> = mutableMapOf()
    fun addNotification(key: String, icon: Identifier, title: Text, body: Text?, buttons: List<Button>): Int {
        val mc = MinecraftClient.getInstance()
        if (dontShowAgain.contains(key)) return -1
        Reden.LOGGER.info("Adding message $key")
        mc.toastManager.add(
            SystemToast.create(
                mc,
                toastType,
                title,
                body ?: Text.literal("Press " + OPEN_NOTIFICATIONS_SCREEN.stringValue + " for more info.")
            )
        )
        nextId++
        messages[nextId] = Message(
            icon,
            title,
            body ?: Text.empty(),
            buttons
        )
        return nextId
    }

    fun onceNotification(key: String, icon: Identifier, title: Text, body: Text, buttons: List<Button>): Int {
        if (temporaryDontShow.contains(key)) return -1
        temporaryDontShow.add(key)
        return addNotification(key, icon, title, body, buttons)
    }

    fun dontShowAgain(key: String) {
        dontShowAgain.add(key)
        temporaryDontShow.add(key)
        dontShowAgainConfig.writeText(dontShowAgain.joinToString("\n"))
    }
    fun remove(id: Int) {
        messages.remove(id)
        val mc = MinecraftClient.getInstance()
        val screen = mc.currentScreen
        if (screen is Screen) {
            mc.setScreen(Screen(screen.parent))
        }
    }

    class Screen(
        val parent: net.minecraft.client.gui.screen.Screen?
    ) : BaseOwoScreen<ScrollContainer<FlowLayout>>() {
        override fun createAdapter(): OwoUIAdapter<ScrollContainer<FlowLayout>> {
            return OwoUIAdapter.create(this) { h, v ->
                Containers.verticalScroll(Sizing.fill(), Sizing.fill(), Containers.verticalFlow(h, v))
            }
        }

        override fun build(rootComponent: ScrollContainer<FlowLayout>?) {
            val root = rootComponent?.child() ?: return
            root.horizontalAlignment(HorizontalAlignment.RIGHT)
            root.surface(Surface.VANILLA_TRANSLUCENT)
            messages.values.forEach {
                root.child(it)
            }
        }

        override fun close() {
            client?.setScreen(parent)
        }
    }

    fun openScreen() {
        val mc = MinecraftClient.getInstance()
        mc.setScreen(Screen(mc.currentScreen))
    }
}
