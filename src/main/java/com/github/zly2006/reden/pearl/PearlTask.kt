package com.github.zly2006.reden.pearl

import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import com.github.zly2006.reden.malilib.HOTKEYS
import com.github.zly2006.reden.network.TntSyncPacket
import com.github.zly2006.reden.utils.sendMessage
import fi.dy.masa.malilib.config.options.ConfigHotkey
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient

var pearlTask: PearlTask? = null

/**
 * Client-side
 */
class PearlTask {
    val pearlEntity = MyEndPearlEntity()
    var pearlPosSaved = false
    var flags = 0
    val tnts = Array<MyTnt?>(4) { null }
    enum class Mode(
        val pearlState: Int,
        val acceptPackets: Boolean
    ) {
        NOT_INITIALIZED(0, false),
        RECORDING(0, true),
        NE(1, true),
        NW(2, true),
        SE(4, true),
        SW(8, true),
        PEARL(16, true),
        CALCULATING(-1, false),
        FINISHED(-1, false),
    }
    var mode = Mode.NOT_INITIALIZED
    companion object {
        val masaHotkeyConfig = ConfigHotkey("pearlHotkey", "P,R", "")
        fun register() {
            masaHotkeyConfig.keybind.setCallback { _, _ ->
                val mc = MinecraftClient.getInstance()
                mc.execute {
                    mc.setScreen(PearlScreen())
                }
                true
            }
            HOTKEYS.add(masaHotkeyConfig)
        }
        init {
            ServerTickEvents.START_SERVER_TICK.register { pearlTask?.tickStart() }
        }
    }

    fun onTntSyncPacket(packet: TntSyncPacket) {
        if (!mode.acceptPackets) return
        if (!pearlPosSaved || packet.projectilePos != pearlEntity.pos) {
            pearlEntity.pos.set(packet.projectilePos)
            pearlEntity.motion.set(packet.projectileMotion)
            pearlPosSaved = true
            flags = 16
            tnts.fill(null)
            if (DEBUG_LOGGER.booleanValue) {
                MinecraftClient.getInstance().player?.sendMessage("PearlTask: refresh pearl pos")
            }
        }
        else if (DEBUG_LOGGER.booleanValue && pearlEntity.motion != packet.projectileMotion && !pearlPosSaved) {
            MinecraftClient.getInstance().player?.sendMessage("PearlTask: pearl motion changed, and it is not caused by previous explosion.")
        }
        if (packet.tntPos.distanceTo(packet.projectilePos) < 1.5) {
            // north-east = 0, north-west = 1, south-east = 2, south-west = 3
            val index =
                (if (packet.tntPos.x > packet.projectilePos.x) 0 else 1) +
                (if (packet.tntPos.z > packet.projectilePos.z) 0 else 2)
            if (tnts[index] == null) {
                tnts[index] = MyTnt(packet.tntPos, 4)
                flags = flags or (1 shl index)
                if (DEBUG_LOGGER.booleanValue) {
                    MinecraftClient.getInstance().player?.sendMessage("PearlTask: set tnt pos $index")
                }
            }
            else {
                if (DEBUG_LOGGER.booleanValue) {
                    if (tnts[index]!!.pos != packet.tntPos) {
                        MinecraftClient.getInstance().player?.sendMessage("PearlTask: tnt pos error $index")
                    }
                }
            }
        }
        if (flags == 31) {
            mode = Mode.CALCULATING
            MinecraftClient.getInstance().player?.sendMessage("PearlTask: start calculating")
        }
    }

    fun tickStart() {
        pearlPosSaved = false
    }
}