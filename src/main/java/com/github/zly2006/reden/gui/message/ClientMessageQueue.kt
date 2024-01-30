package com.github.zly2006.reden.gui.message

import com.github.zly2006.reden.malilib.OPEN_NOTIFICATIONS_SCREEN
import fi.dy.masa.malilib.util.FileUtils
import io.wispforest.owo.ui.base.BaseOwoToast
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.time.Duration

object ClientMessageQueue {
    private val dontShowAgain: MutableSet<String> = mutableSetOf()
    private val dontShowAgainConfig = FileUtils.getConfigDirectory().resolve("reden/notifications_dont_show_again.txt")

    init {
        if (dontShowAgainConfig.exists()) {
            dontShowAgainConfig.readLines().forEach {
                dontShowAgain.add(it)
            }
        }
    }

    private val toastType = SystemToast.Type()

    @Suppress("UnstableApiUsage")
    class RedenToast(
        val icon: Identifier,
        val title: Text,
        val description: Text,
        val textureSize: Int = 16
    ) : BaseOwoToast<FlowLayout>({
        val root = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(100))
        root.child(Components.label(Text.literal("Reden Notification")).apply {

        })
        root.surface(
            Surface.PANEL
        )
        root
    }, Duration.ofSeconds(5)) {

    }
    class Button(
        val message: Text,
        val action: () -> Unit
    )
    data class Message(
        val icon: Identifier,
        val title: Text,
        val body: Text,
        val buttons: List<Button>
    )
    val messages: MutableList<Message> = mutableListOf()
    fun add(icon: Identifier, title: Text, body: Text?, buttons: List<Button>): Int {
        val mc = MinecraftClient.getInstance()
        SystemToast.show(
            mc.toastManager,
            toastType,
            title,
            body ?: Text.literal("Press " + OPEN_NOTIFICATIONS_SCREEN.stringValue + " for more info.")
        )
        // todo
        return -1
    }

    fun onceNotification(key: String, icon: Identifier, title: Text, body: Text, buttons: List<Button>) {
        if (dontShowAgain.contains(key)) return
        add(icon, title, body, buttons)
    }

    fun dontShowAgain(key: String) {
        dontShowAgain.add(key)
        dontShowAgainConfig.writeText(dontShowAgain.joinToString("\n"))
    }
    fun remove(id: Int) {
        messages.removeAt(id)
        // todo: refresh screen
    }
    fun openScreen() {
        TODO()
    }
}
