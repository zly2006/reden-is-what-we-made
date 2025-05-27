package com.github.zly2006.reden

import com.github.zly2006.reden.malilib.HOTKEYS
import com.github.zly2006.reden.malilib.configureKeyCallbacks
import com.github.zly2006.reden.malilib.getAllOptions
import com.github.zly2006.reden.network.registerClientPackets
import com.github.zly2006.reden.utils.checkMalilib
import com.github.zly2006.reden.utils.isDebug
import com.github.zly2006.reden.utils.startDebugAppender
import com.google.gson.Gson
import com.google.gson.JsonObject
import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.event.InitializationHandler
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.util.FileUtils
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import java.nio.file.Files
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists

val GSON = Gson()

fun loadMalilibSettings() {
    val path = FileUtils.getConfigDirectoryAsPath().resolve("reden/config.json")
        .createParentDirectories()
    if (!path.exists()) {
        return
    }
    val jo = GSON.fromJson(Files.readString(path), JsonObject::class.java)
    ConfigUtils.readConfigBase(jo, Reden.MOD_NAME, getAllOptions())
    if (isDebug) {
        startDebugAppender()
    }
}

fun saveMalilibOptions() {
    val jo = JsonObject()
    ConfigUtils.writeConfigBase(jo, Reden.MOD_NAME, getAllOptions())
    Files.writeString(
        FileUtils.getConfigDirectoryAsPath().resolve("reden/config.json")
            .createParentDirectories(),
        GSON.toJson(jo)
    )
}

class RedenClient : ClientModInitializer {
    override fun onInitializeClient() {
        checkMalilib()
        registerClientPackets()
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
            val mc = Minecraft.getInstance()
            configureKeyCallbacks(mc)

            InputEventHandler.getKeybindManager().registerKeybindProvider(object : IKeybindProvider {
                override fun addKeysToMap(iKeybindManager: IKeybindManager) {
                    HOTKEYS.forEach {
                        iKeybindManager.addKeybindToMap(it.keybind)
                    }
                }

                override fun addHotkeys(keybindManager: IKeybindManager) {
                    keybindManager.addHotkeysForCategory("Reden", "reden.hotkeys.category.generic_hotkeys", HOTKEYS)
                }
            })
        }
    }
}
