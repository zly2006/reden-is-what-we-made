package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Sounds
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.gui.CreditScreen
import com.github.zly2006.reden.mixinhelper.StructureBlockHelper
import com.github.zly2006.reden.network.RvcDataS2CPacket
import com.github.zly2006.reden.network.RvcTrackpointsC2SRequest
import com.github.zly2006.reden.network.Undo
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.gui.SelectionListScreen
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.remote.github.GithubAuthScreen
import com.github.zly2006.reden.sponsor.SponsorScreen
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.toBlockPos
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
    UNDO_KEY.keybind.setCallback { _, _ ->
        onFunctionUsed("undo")
        val playSound = Random.nextInt(100) < EASTER_EGG_RATE.integerValue
        if (playSound) mc.world!!.playSound(
            mc.player,
            mc.player!!.blockPos,
            Sounds.THE_WORLD,
            SoundCategory.BLOCKS
        )
        iEVER_USED_UNDO.booleanValue = true
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Undo(0))
            true
        } else false
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
}