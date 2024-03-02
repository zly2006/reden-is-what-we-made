package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.Sounds
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.gui.BreakpointInfoScreen
import com.github.zly2006.reden.debugger.gui.BreakpointListComponent
import com.github.zly2006.reden.gui.CreditScreen
import com.github.zly2006.reden.gui.message.ClientMessageQueue
import com.github.zly2006.reden.mixinhelper.StructureBlockHelper
import com.github.zly2006.reden.network.*
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.report.reportException
import com.github.zly2006.reden.rvc.gui.SelectionImportScreen
import com.github.zly2006.reden.rvc.gui.SelectionListScreen
import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.remote.github.GithubAuthScreen
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.sponsor.SponsorScreen
import com.github.zly2006.reden.task.taskStack
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.toBlockPos
import com.github.zly2006.reden.utils.translateMessage
import com.github.zly2006.reden.wormhole.Wormhole
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.hud.Hud
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.block.Blocks
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.block.enums.StructureBlockMode
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import org.lwjgl.glfw.GLFW
import java.util.zip.ZipInputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.callback {
        mc.setScreen(GuiConfigs())
        true
    }
    var undoEasterEggLock = false
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
            mc.world!!.playSound(
                mc.player,
                mc.player!!.blockPos,
                Sounds.THE_WORLD,
                SoundCategory.BLOCKS
            )
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
    REDO_KEY.callback {
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Undo(1))
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.callback {
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
    DEBUG_PREVIEW_UNDO.callback {
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            BlockBorder.tags.clear()
            val view = mc.server!!.playerManager.playerList[0].data()
            view.undo.lastOrNull()?.data?.keys?.forEach {
                BlockBorder.tags[it] = 1
            }
            return@callback true
        }
        return@callback false
    }
    OPEN_GITHUB_AUTH_SCREEN.callback {
        onFunctionUsed("rvc.github")
        mc.setScreen(GithubAuthScreen())
        true
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
    OPEN_IMPORT_SCREEN.callback {
        mc.setScreen(SelectionImportScreen())
        true
    }
    var selectedWormhole: Wormhole? = null
    WORMHOLE_SELECT.callback {
        selectedWormhole = null
        BlockOutline.blocks.clear()
        true
    }
    ClientTickEvents.START_CLIENT_TICK.register {
        if (mc.player != null) {
            val cosPitch = abs(cos(Math.toRadians(mc.player!!.pitch.toDouble())))
            val pos = Vec3d(
                -sin(Math.toRadians(mc.player!!.yaw.toDouble())) * cosPitch,
                -sin(Math.toRadians(mc.player!!.pitch.toDouble())),
                cos(Math.toRadians(mc.player!!.yaw.toDouble())) * cosPitch
            ).normalize()

            fun eval(it: Wormhole): Double {
                /*
            val xOyDistance = mc.player!!.eyePos.withAxis(Direction.Axis.Y, 0.0)
                .distanceTo(it.destination.toCenterPos().withAxis(Direction.Axis.Y, 0.0))
            val yaw = Math.toDegrees(atan2(-(it.destination.toCenterPos().x - mc.player!!.eyePos.x), (it.destination.toCenterPos().z - mc.player!!.eyePos.z)))
            val pitch = Math.toDegrees(atan2(abs(mc.player!!.eyePos.y - it.destination.toCenterPos().y), xOyDistance))

            val d = abs((yaw - mc.player!!.yaw).mod(360.0)) + abs((pitch - mc.player!!.pitch).mod(360.0))
             */
                val d = pos.distanceTo(it.destination.toCenterPos().subtract(mc.player!!.eyePos).normalize())
                return d
            }
            if (WORMHOLE_SELECT.keybind.isPressed) {
                selectedWormhole = null
                BlockOutline.blocks.clear()
                mc.data.wormholes.minByOrNull(::eval)?.let {
                    if (eval(it) > 0.4) return@let
                    selectedWormhole = it
                    BlockOutline.blocks[it.destination] = Blocks.STONE.defaultState
                }
            }
        }
    }
    InputEventHandler.getInputManager().registerMouseInputHandler(object : IMouseInputHandler {
        override fun onMouseClick(mouseX: Int, mouseY: Int, eventButton: Int, eventButtonState: Boolean): Boolean {
            if (!WORMHOLE_SELECT.keybind.isPressed) return false
            if (eventButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                mc.data.wormholes.removeIf { it.destination == (mc.crosshairTarget as? BlockHitResult?)?.blockPos }
                mc.data.wormholes.add(
                    Wormhole(
                        (mc.crosshairTarget as? BlockHitResult?)?.blockPos ?: return false,
                        "Wormhole 1",
                        mc.player?.pos ?: return false,
                        mc.player?.yaw ?: return false,
                        mc.player?.pitch ?: return false,
                    )
                )
                return true
            } else if (eventButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                val wormhole = selectedWormhole ?: return false
                if (mc.server == null) {
                    mc.player!!.sendMessage("You can not use this in a server")
                    return false
                }
                mc.server!!.playerManager.getPlayer(mc.player!!.uuid)!!
                    .teleport(wormhole.tpPosition.x, wormhole.tpPosition.y, wormhole.tpPosition.z)
                mc.player!!.yaw = wormhole.tpYaw
                mc.player!!.pitch = wormhole.tpPitch
                return true
            }
            return false
        }
    })
    OPEN_SELECTION_LIST.callback {
        mc.setScreen(SelectionListScreen())
        true
    }
    DEBUG_RVC_REQUEST_SYNC_DATA.callback {
        ClientPlayNetworking.send(RvcTrackpointsC2SRequest(1, selectedStructure!!))
        RvcDataS2CPacket.consumer = {
            val rootFile = mc.runDirectory.resolve("DEBUG_RVC_REQUEST_SYNC_DATA").normalize()
            ZipInputStream(it.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    print(name)
                    val file = rootFile.resolve(name).normalize()
                    if (!file.startsWith(rootFile)) {
                        Reden.LOGGER.error("Zip entry $name is outside of root directory")
                        continue
                    }
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
    PAUSE_KEY.callback {
        ClientPlayNetworking.send(Pause(true))
        true
    }
    CONTINUE_KEY.callback {
        ClientPlayNetworking.send(Continue())
        true
    }
    STEP_INTO_KEY.callback {
        ClientPlayNetworking.send(StepInto())
        true
    }
    STEP_OVER_KEY.callback {
        val id = mc.serverData?.tickStageTree?.activeStage?.id
        if (id == null) return@callback false
        else ClientPlayNetworking.send(StepOver(id))
        true
    }
    VIEW_ALL_BREAKPOINTS.callback {
        mc.setScreen(BreakpointListComponent.Screen(mc.data.breakpoints.breakpointMap.values))
        true
    }
    val pointTypes = BreakpointsManager.getBreakpointManager().registry.values.toList()
    var index = 0
    ADD_BREAKPOINT.callback {
        if (mc.crosshairTarget?.type != HitResult.Type.BLOCK) return@callback false
        val pos = (mc.crosshairTarget as? BlockHitResult?)?.blockPos ?: return@callback false
        val manager = mc.data.breakpoints
        manager.createBreakpointDefault(
            pointTypes[index],
            mc.world!!,
            pos
        )
        BlockBorder[pos] = TagBlockPos.green
        true
    }
    EDIT_BREAKPOINTS.callback {
        val breakpoints = mc.data.breakpoints.breakpointMap.values.filter {
            it.world == mc.world?.registryKey?.value && it.pos == mc.crosshairTarget?.pos?.toBlockPos()
        }.ifEmpty {
            mc.player?.sendMessage("Not found")
            return@callback true
        }
        if (breakpoints.size == 1)
            mc.setScreen(BreakpointInfoScreen(breakpoints.first()))
        else
            mc.setScreen(BreakpointListComponent.Screen(breakpoints))
        true
    }
    ScreenEvents.BEFORE_INIT.register { _, _, _, _ ->
        BREAKPOINT_RENDERER.booleanValue = false
    }
    BREAKPOINT_RENDERER.setValueChangeCallback {
        if (it.booleanValue) {
            Hud.add(Reden.identifier("breakpoint-tutorial")) {
                Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    surface(Surface.TOOLTIP)
                    padding(Insets.of(6))
                    gap(3)
                    positioning(Positioning.across(50, 60))
                    fun format(text: String) = Text.empty().append(Text.literal(text.replace(",", " + "))
                        .formatted(Formatting.GOLD))
                    child(Components.label(format(EDIT_BREAKPOINTS.stringValue).append(" to edit breakpoints")))
                    child(Components.label(format(BREAKPOINT_RENDERER.keybind.stringValue + " + Scroll").append(" to change breakpoint type")))
                    child(Components.label(format(ADD_BREAKPOINT.stringValue).append(" to add breakpoints")))
                    child(Components.label(format(VIEW_ALL_BREAKPOINTS.stringValue).append(" to view all breakpoints")))
                }
            }
        } else Hud.remove(Reden.identifier("breakpoint-tutorial"))
    }
    InputEventHandler.getInputManager().registerMouseInputHandler(object : IMouseInputHandler {
        var scrollStartTime = 0L
        override fun onMouseScroll(mouseX: Int, mouseY: Int, amount: Double): Boolean {
            if (BREAKPOINT_RENDERER.booleanValue) {
                if (System.currentTimeMillis() - scrollStartTime < 250) return false
                scrollStartTime = System.currentTimeMillis()
                index += amount.sign.toInt()
                index = index.mod(pointTypes.size)
                val type = pointTypes[index]
                mc.player?.sendMessage(Text.literal("Type now is ").append(type.description))
                return true
            } else return false
        }
    })
    DEBUG_DISPLAY_RVC_WORLD_INFO.callback {
        val info = mc.getWorldInfo()
        MinecraftClient.getInstance().player?.sendMessage(Text.literal(Json.encodeToString(info)))
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
    RVC_CONFIRM_KEY.callback { taskStack.last().onConfirm() }
    RVC_CANCEL_KEY.callback { taskStack.last().onCancel() }
    DEBUG_LITEMATICA_SCHEMATIC_RERENDER.callback {
        (taskStack.last() as RvcMoveStructureLitematicaTask).apply {
            currentOrigin = currentOrigin
        }
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
