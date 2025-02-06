package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.Sounds
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.gui.CreditScreen
import com.github.zly2006.reden.gui.message.ClientMessageQueue
import com.github.zly2006.reden.minemev.MevScreen
import com.github.zly2006.reden.mixinhelper.StructureBlockHelper
import com.github.zly2006.reden.network.Undo
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.report.reportException
import com.github.zly2006.reden.sponsor.SponsorScreen
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.toBlockPos
import com.github.zly2006.reden.utils.translateMessage
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.gui.GuiConfigsBase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.block.enums.StructureBlockMode
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.AbstractSoundInstance
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import kotlin.random.Random

fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.callback {
        mc.setScreen(GuiConfigs())
        true
    }
    var undoEasterEggLock = false
    val zawaludo = object : AbstractSoundInstance(
        Sounds.THE_WORLD.id,
        SoundCategory.VOICE,
        net.minecraft.util.math.random.Random.create()
    ) {}
    UNDO_KEY.callback {
        if (undoEasterEggLock) {
            mc.player?.sendMessage(translateMessage("undo", "busy"))
            return@callback false
        }
        if (mc.serverData?.featureSet?.contains("undo") != true) {
            mc.player?.sendMessage(Text.literal("Sorry, this server doesn't support undo.").red(), true)
            return@callback false
        }
        ClientMessageQueue.dontShowAgain("reden:undo")
        if (mc.interactionManager?.currentGameMode != GameMode.CREATIVE)
            return@callback false
        val playSound = Random.nextInt(100) < EASTER_EGG_RATE.integerValue
        if (playSound) {
            mc.soundManager.play(zawaludo)
            undoEasterEggLock = true
            if (!EASTER_EGG_RATE.isModified) {
                val key = "reden:easter_egg/the_world"
                ClientMessageQueue.remove(
                    ClientMessageQueue.onceNotification(
                        key,
                        Reden.identifier("textures/gui/notification.png"),
                        Text.literal("Easter eggs..."),
                        Text.literal("You can turn it off at this setting: easterEggRate"),
                        listOf()
                    )
                )
            }
            GlobalScope.launch(mc.asCoroutineDispatcher()) {
                delay(2000)
                undoEasterEggLock = false
                ClientPlayNetworking.send(Undo(0))
            }
        }
        else
            ClientPlayNetworking.send(Undo(0))
        true
    }
    REDO_KEY.callback {
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Undo(1))
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.callback {
        val pos = mc.crosshairTarget?.pos?.toBlockPos()
        if (pos != null) {
            val new = when (val old = BlockBorder[pos]) {
                3 -> 0
                else -> old + 1
            }
            BlockBorder[pos] = new
            mc.player?.sendMessage("OK $pos=$new")
            true
        }
        else false
    }
    DEBUG_PREVIEW_UNDO.callback {
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE && mc.server != null) {
            BlockBorder.tags = mutableMapOf()
            val view = mc.server!!.playerManager.playerList[0].data()
            view.undo.lastOrNull()?.data?.keys?.forEach {
                (BlockBorder.tags as MutableMap)[it] = 1
            }
            return@callback true
        }
        return@callback false
    }
    STRUCTURE_BLOCK_LOAD.callback {
        if (StructureBlockHelper.isValid) {
            val structureBlock = mc.world!!.getBlockEntity(StructureBlockHelper.lastUsed!!) as StructureBlockBlockEntity
            structureBlock.mode = StructureBlockMode.LOAD
            mc.networkHandler?.sendPacket(
                UpdateStructureBlockC2SPacket(
                    structureBlock.pos,
                    StructureBlockBlockEntity.Action.LOAD_AREA,
                    structureBlock.mode,
                    structureBlock.templateName,
                    structureBlock.offset,
                    structureBlock.size,
                    structureBlock.mirror,
                    structureBlock.rotation,
                    structureBlock.metadata,
                    structureBlock.shouldIgnoreEntities(),
                    structureBlock.shouldShowAir(),
                    structureBlock.shouldShowBoundingBox(),
                    structureBlock.integrity,
                    structureBlock.seed
                )
            )
        }
        true
    }
    STRUCTURE_BLOCK_SAVE.callback {
        if (StructureBlockHelper.isValid) {
            val structureBlock = mc.world!!.getBlockEntity(StructureBlockHelper.lastUsed!!) as StructureBlockBlockEntity
            structureBlock.mode = StructureBlockMode.SAVE
            mc.networkHandler?.sendPacket(
                UpdateStructureBlockC2SPacket(
                    structureBlock.pos,
                    StructureBlockBlockEntity.Action.SAVE_AREA,
                    structureBlock.mode,
                    structureBlock.templateName,
                    structureBlock.offset,
                    structureBlock.size,
                    structureBlock.mirror,
                    structureBlock.rotation,
                    structureBlock.metadata,
                    structureBlock.shouldIgnoreEntities(),
                    structureBlock.shouldShowAir(),
                    structureBlock.shouldShowBoundingBox(),
                    structureBlock.integrity,
                    structureBlock.seed
                )
            )
        }
        true
    }
    SPONSOR_SCREEN_KEY.callback {
        mc.setScreen(SponsorScreen())
        true
    }
    CREDIT_SCREEN_KEY.callback {
        mc.setScreen(CreditScreen())
        true
    }
    DEBUG_VIEW_ALL_CONFIGS.callback {
        mc.setScreen(object : GuiConfigsBase(
            10,
            20,
            Reden.MOD_ID,
            null,
            "reden.widget.config.title") {
            override fun getConfigs() = ConfigOptionWrapper.createFor(getAllOptions())
        })
        true
    }
    OPEN_NOTIFICATIONS_SCREEN.callback {
        ClientMessageQueue.openScreen()
        true
    }
    DEBUG_NEW_NOTIFICATION.callback {
        var id = 0
        id = ClientMessageQueue.addNotification(
            "reden:debug",
            Reden.identifier("textures/gui/notification.png"),
            Text.literal("Test"),
            Text.literal("Test"),
            listOf(
                ClientMessageQueue.Button(Text.literal("OK")) {
                    ClientMessageQueue.remove(id)
                }
            )
        )
        true
    }
    DEBUG_MINENV_GUI.callback {
        mc.setScreen(MevScreen())
        true
    }
}

private fun ConfigHotkey.callback(action: () -> Boolean) {
    keybind.setCallback { _, _ ->
        try {
            if (action()) {
                onFunctionUsed(name)
                true
            } else false
        } catch (e: Exception) {
            Reden.LOGGER.error("Error when executing hotkey $name", e)
            reportException(e)
            MinecraftClient.getInstance().player?.sendMessage(Text.literal("Error when executing hotkey $name").red())
            false
        }
    }
}
