package com.github.zly2006.reden

import com.github.zly2006.reden.clientGlow.registerClientGlow
import com.github.zly2006.reden.malilib.HOTKEYS
import com.github.zly2006.reden.malilib.RUN_COMMAND
import com.github.zly2006.reden.malilib.configureKeyCallbacks
import com.github.zly2006.reden.malilib.getAllOptions
import com.github.zly2006.reden.report.redenSetup
import com.github.zly2006.reden.sponsor.LuckToday.Companion.luckValue
import com.github.zly2006.reden.utils.checkMalilib
import com.github.zly2006.reden.utils.isDebug
import com.github.zly2006.reden.utils.startDebugAppender
import com.github.zly2006.reden.yo.registerGreenstone
import com.google.gson.JsonObject
import com.redenmc.bragadier.ktdsl.register
import com.redenmc.bragadier.ktdsl.then
import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.event.InitializationHandler
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.util.FileUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import java.io.File
import java.nio.file.Files

fun loadMalilibSettings() {
    val file = File(FileUtils.getConfigDirectory(), Reden.CONFIG_FILE)
    if (!file.exists()) {
        return
    }
    val jo = Reden.GSON.fromJson(Files.readString(file.toPath()), JsonObject::class.java)
    ConfigUtils.readConfigBase(jo, Reden.MOD_NAME, getAllOptions())
    if (isDebug) {
        startDebugAppender()
    }
}

fun saveMalilibOptions() {
    val jo = JsonObject()
    File(FileUtils.getConfigDirectory(), "reden").mkdirs()
    ConfigUtils.writeConfigBase(jo, Reden.MOD_NAME, getAllOptions())
    Files.writeString(
        File(FileUtils.getConfigDirectory(), Reden.CONFIG_FILE).toPath(),
        Reden.GSON.toJson(jo)
    )
}

class RedenClient : ClientModInitializer {
    override fun onInitializeClient() {
        checkMalilib()
        registerGreenstone()
        InitializationHandler.getInstance().registerInitializationHandler {
            ConfigManager.getInstance().registerConfigHandler("reden", object : IConfigHandler {
                override fun load() {
                    loadMalilibSettings()
                }

                override fun save() {
                    saveMalilibOptions()
                }
            })
            loadMalilibSettings()
            InputEventHandler.getKeybindManager().registerKeybindProvider(object : IKeybindProvider {
                override fun addKeysToMap(iKeybindManager: IKeybindManager) {
                    HOTKEYS.forEach { iKeybindManager.addKeybindToMap(it.keybind) }

                    for ((commands, keybind) in RUN_COMMAND.commandHotkeyList) {
                        iKeybindManager.addKeybindToMap(keybind)
                        keybind.setCallback { _, _ ->
                            val networkHandler = MinecraftClient.getInstance().networkHandler
                            if (networkHandler != null) {
                                for (command in commands) {
                                    if (command.startsWith("/")) {
                                        networkHandler.sendChatCommand(command.substring(1))
                                    }
                                    else {
                                        networkHandler.sendChatMessage(command)
                                    }
                                }
                                return@setCallback true
                            }
                            false
                        }
                    }
                }

                override fun addHotkeys(iKeybindManager: IKeybindManager) {
                    iKeybindManager.addHotkeysForCategory("Reden", "reden.hotkeys.category.generic_hotkeys", HOTKEYS)
                }
            })
            configureKeyCallbacks(MinecraftClient.getInstance())
        }
        ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { client -> redenSetup(client) })
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, registryAccess ->
            registerClientGlow(dispatcher)
            dispatcher.register {
                literal("reden-debug-client").then {
                    literal("floating-item").then {
                        argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes { context ->
                            val client = MinecraftClient.getInstance()
                            client.gameRenderer.showFloatingItem(
                                ItemStackArgumentType.getItemStackArgument(
                                    context,
                                    "item"
                                ).createStack(1, false)
                            )
                            assert(client.world != null)
                            assert(client.player != null)
                            client.world!!.playSound(
                                client.player!!.x,
                                client.player!!.y,
                                client.player!!.z,
                                SoundEvents.ITEM_TOTEM_USE,
                                client.player!!.soundCategory,
                                1.0f,
                                1.0f,
                                false
                            )
                            1
                        }
                    }
                    literal("luck-today").executes { context ->
                        context.source.sendFeedback(
                            Text.literal(luckValue.data.toString())
                        )
                        1
                    }
                }
            }
        })
    }
}
