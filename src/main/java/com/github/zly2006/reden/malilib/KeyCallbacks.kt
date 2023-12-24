package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.Sounds
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.debugger.gui.BreakpointInfoScreen
import com.github.zly2006.reden.debugger.gui.BreakpointListComponent
import com.github.zly2006.reden.gui.CreditScreen
import com.github.zly2006.reden.mixinhelper.StructureBlockHelper
import com.github.zly2006.reden.network.*
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.gui.SelectionListScreen
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.remote.github.GithubAuthScreen
import com.github.zly2006.reden.sponsor.SponsorScreen
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.toBlockPos
import com.github.zly2006.reden.utils.translateMessage
import fi.dy.masa.malilib.gui.GuiConfigsBase
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.block.enums.StructureBlockMode
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.util.zip.ZipInputStream
import kotlin.random.Random

fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.keybind.setCallback { _, _ ->
        mc.setScreen(GuiConfigs())
        true
    }
    var undoEasterEggLock = false
    UNDO_KEY.keybind.setCallback { _, _ ->
        if (undoEasterEggLock) {
            mc.player?.sendMessage(translateMessage("undo", "busy"))
            return@setCallback false
        }
        if (mc.serverData?.featureSet?.contains("undo") != true) {
            mc.player?.sendMessage(Text.literal("Sorry, this server doesn't support undo.").red(), true)
            return@setCallback false
        }
        if (mc.interactionManager?.currentGameMode != GameMode.CREATIVE)
            return@setCallback false
        onFunctionUsed("undo")
        iEVER_USED_UNDO.booleanValue = true
        val playSound = Random.nextInt(100) < EASTER_EGG_RATE.integerValue
        if (playSound) {
            mc.world!!.playSound(
                mc.player,
                mc.player!!.blockPos,
                Sounds.THE_WORLD,
                SoundCategory.BLOCKS
            )
            undoEasterEggLock = true
            Thread {
                Thread.sleep(2000)
                undoEasterEggLock = false
                ClientPlayNetworking.send(Undo(0))
            }.start()
        }
        else
            ClientPlayNetworking.send(Undo(0))
        true
    }
    REDO_KEY.keybind.setCallback { _, _ ->
        onFunctionUsed("redo")
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Undo(1))
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.keybind.setCallback { _, _ ->
        val pos = mc.crosshairTarget?.pos?.toBlockPos()
        if (pos != null) {
            val new = BlockBorder.tags.compute(pos.asLong()) { _, old ->
                when (old) {
                    3 -> 0
                    null -> 1
                    else -> old + 1
                }
            }
            mc.player?.sendMessage("OK $pos=$new")
            true
        } else false
    }
    DEBUG_PREVIEW_UNDO.keybind.setCallback { _, _ ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            BlockBorder.tags.clear()
            val view = mc.server!!.playerManager.playerList[0].data()
            view.undo.lastOrNull()?.data?.keys?.forEach {
                BlockBorder.tags[it] = 1
            }
            return@setCallback true
        }
        return@setCallback false
    }
    OPEN_GITHUB_AUTH_SCREEN.keybind.setCallback { _, _ ->
        onFunctionUsed("rvc.github")
        mc.setScreen(GithubAuthScreen())
        true
    }
    STRUCTURE_BLOCK_LOAD.keybind.setCallback { _, _ ->
        onFunctionUsed("structure_block.load")
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
    STRUCTURE_BLOCK_SAVE.keybind.setCallback { _, _ ->
        onFunctionUsed("structure_block.save")
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
    OPEN_SELECTION_LIST.keybind.setCallback { _, _ ->
        mc.setScreen(SelectionListScreen())
        true
    }
    DEBUG_RVC_REQUEST_SYNC_DATA.keybind.setCallback { _, _ ->
        ClientPlayNetworking.send(RvcTrackpointsC2SRequest(
            selectedStructure?.trackPoints ?: listOf(),
            1,
            "DEBUG_RVC_REQUEST_SYNC_DATA"
        ))
        RvcDataS2CPacket.consumer = {
            ZipInputStream(it.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    print(name)
                    val file = mc.runDirectory.resolve("DEBUG_RVC_REQUEST_SYNC_DATA").resolve(name)
                    file.parentFile.mkdirs()
                    file.writeBytes(zip.readAllBytes())
                    entry = zip.nextEntry
                    print(file.absolutePath)
                }
            }
        }
        mc.messageHandler.onGameMessage(Text.literal("DEBUG_RVC_REQUEST_SYNC_DATA"), false)
        true
    }
    SPONSOR_SCREEN_KEY.keybind.setCallback { _, _ ->
        mc.setScreen(SponsorScreen())
        true
    }
    CREDIT_SCREEN_KEY.keybind.setCallback { _, _ ->
        mc.setScreen(CreditScreen())
        true
    }
    DEBUG_VIEW_ALL_CONFIGS.keybind.setCallback { _, _ ->
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
    val pointTypes = BreakpointsManager.getBreakpointManager().registry.values.toList()
    var index = 0
    ADD_BREAKPOINT.keybind.setCallback { _, _ ->
        val pos = mc.crosshairTarget?.pos?.toBlockPos() ?: return@setCallback false
        val type = pointTypes[index]
        val manager = mc.data.breakpoints
        val id = (manager.breakpointMap.keys.maxOrNull() ?: 0) + 1
        manager.breakpointMap[id] = type.create(id).apply {
            world = mc.world!!.registryKey.value
            setPosition(pos)
            handler.add(BreakPoint.Handler(FreezeGame(), name = "Behavior 1"))
        }
        mc.data.breakpoints.sync(manager.breakpointMap[id])
        true
    }
    CYCLE_BREAKPOINT_TYPE.keybind.setCallback { _, _ ->
        index++
        if (index !in pointTypes.indices) index = 0
        val type = pointTypes[index]
        mc.player?.sendMessage(Text.literal("Type now is ").append(type.description))
        true
    }
    EDIT_BREAKPOINTS.keybind.setCallback { _, _ ->
        val breakpoints = mc.data.breakpoints.breakpointMap.values.filter {
            it.world == mc.world?.registryKey?.value && it.pos == mc.crosshairTarget?.pos?.toBlockPos()
        }.ifEmpty {
            mc.player?.sendMessage("Not found")
            return@setCallback false
        }
        if (breakpoints.size == 1)
            mc.setScreen(BreakpointInfoScreen(breakpoints.first()))
        else
            mc.setScreen(BreakpointListComponent.Screen(breakpoints))
        true
    }
    PAUSE_KEY.keybind.setCallback { _, _ ->
        ClientPlayNetworking.send(Pause(true))
        true
    }
    CONTINUE_KEY.keybind.setCallback { _, _ ->
        ClientPlayNetworking.send(Continue())
        true
    }
    STEP_INTO_KEY.keybind.setCallback { _, _ ->
        ClientPlayNetworking.send(StepInto())
        true
    }
    STEP_OVER_KEY.keybind.setCallback { _, _ ->
        val id = mc.serverData?.tickStageTree?.activeStage?.id
        if (id == null) return@setCallback false
        else ClientPlayNetworking.send(StepOver(id))
        true
    }
    VIEW_ALL_BREAKPOINTS.keybind.setCallback { _, _ ->
        mc.setScreen(BreakpointListComponent.Screen(mc.data.breakpoints.breakpointMap.values))
        true
    }
}