package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.VisibleChatHudLineAccess
import com.github.zly2006.reden.gui.QuickMenuWidget
import com.github.zly2006.reden.utils.codec.TextSerializer
import com.github.zly2006.reden.utils.multiver.Text
import net.minecraft.client.GuiMessage
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import java.util.regex.Pattern

@Suppress("CAST_NEVER_SUCCEEDS", "HttpUrlsUsage")
object ChatMixinHelper {
    val urlPattern: Pattern = Pattern.compile("(https?://)?[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,8}(/\\S*)?")
    @JvmStatic
    fun initRightClickMenu(menu: QuickMenuWidget, line: GuiMessage.Line, style: Style?) {
        val text = (line as? VisibleChatHudLineAccess)?.text ?: return
        val message: String = text.string
        val matcher = urlPattern.matcher(message)
        val client = Minecraft.getInstance()
        if (matcher.find()) {
            var url = matcher.group()
            if (!url.startsWith("http")) {
                url = "http://$url"
            }
            val finalUrl = url
            menu.addEntry(
                Text.translatable("reden.widget.chat.copy_url")
            ) { entry, button ->
                client.keyboardHandler.clipboard = finalUrl
                entry.setName(Text.translatable("reden.widget.chat.copied"))
            }
        }
        menu.addEntry(
            Text.translatable("reden.widget.chat.copy_raw")
        ) { entry, button ->
            client.keyboardHandler.clipboard = TextSerializer.textToStr(
                text
            )
            entry!!.setName(Text.translatable("reden.widget.chat.copied"))
        }
        menu.addEntry(
            Text.translatable("reden.widget.chat.copy")
        ) { entry, button ->
            client.keyboardHandler.clipboard = message
            entry!!.setName(Text.translatable("reden.widget.chat.copied"))
        }
        if (style != null) {
            if (style.getHoverEvent() != null) {
                //? if < 1.21.5 {
                val action: HoverEvent.Action<*> = style.getHoverEvent()!!.action
                if (action === HoverEvent.Action.SHOW_TEXT) {
                    val hoverText = style.getHoverEvent()!!.getValue(HoverEvent.Action.SHOW_TEXT)!!
                    menu.addEntry(
                        Text.translatable("reden.widget.chat.copy_hover_text")
                    ) { entry, button ->
                        client.keyboardHandler.clipboard = hoverText.string
                    }
                    menu.addEntry(
                        Text.translatable("reden.widget.chat.copy_hover_raw")
                    ) { entry, button ->
                        client.keyboardHandler.clipboard = TextSerializer.textToStr(hoverText)
                        entry!!.setName(Text.translatable("reden.widget.chat.copied"))
                    }
                }
                if (action === HoverEvent.Action.SHOW_ENTITY) {
                    menu.addEntry(
                        Text.translatable("reden.widget.chat.copy_hover_uuid")
                    ) { entry, button ->
                        val uuid = style.getHoverEvent()!!.getValue(HoverEvent.Action.SHOW_ENTITY)?.id
                        client.keyboardHandler.clipboard = uuid.toString()
                        entry.setName(Text.translatable("reden.widget.chat.copied"))
                    }
                }
                //?} else {
                /*when (val event = style.getHoverEvent()) {
                    is HoverEvent.ShowText -> {
                        menu.addEntry(
                            Text.translatable("reden.widget.chat.copy_hover_text")
                        ) { entry, button ->
                            client.keyboardHandler.clipboard = event.value.string
                        }
                        menu.addEntry(
                            Text.translatable("reden.widget.chat.copy_hover_raw")
                        ) { entry, button ->
                            client.keyboardHandler.clipboard = TextSerializer.textToStr(event.value)
                        }
                    }
                    is HoverEvent.ShowEntity -> {
                        menu.addEntry(
                            Text.translatable("reden.widget.chat.copy_hover_uuid")
                        ) { entry, button ->
                            client.keyboardHandler.clipboard = event.entity.uuid.toString()
                        }
                    }
                }
                *///?}
            }
            if (style.getClickEvent() != null) {
                //? if < 1.21.5 {
                if (style.getClickEvent()!!.action == ClickEvent.Action.RUN_COMMAND) {
                    menu.addEntry(
                        Text.translatable("reden.widget.chat.copy_click_command")
                    ) { entry, button ->
                        val command = style.getClickEvent()!!.value
                        client.keyboardHandler.clipboard = command
                        entry.setName(Text.translatable("reden.widget.chat.copied"))
                    }
                }
                if (style.getClickEvent()!!.action == ClickEvent.Action.OPEN_FILE) {
                    menu.addEntry(
                        Text.translatable("reden.widget.chat.copy_click_file")
                    ) { entry, button ->
                        val file = style.getClickEvent()!!.value
                        client.keyboardHandler.clipboard = file
                        entry.setName(Text.translatable("reden.widget.chat.copied"))
                    }
                }
                //?} else {
                /*when (val event = style.getClickEvent()) {
                    is ClickEvent.RunCommand -> {
                        menu.addEntry(
                            Text.translatable("reden.widget.chat.copy_click_command")
                        ) { entry, button ->
                            client.keyboardHandler.clipboard = event.command
                        }
                    }
                    is ClickEvent.OpenFile -> {
                        menu.addEntry(
                            Text.translatable("reden.widget.chat.copy_click_file")
                        ) { entry, button ->
                            client.keyboardHandler.clipboard = event.path
                        }
                    }
                }
                *///?}
            }
        }
    }
}
