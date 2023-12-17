package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.network.SyncBreakpointsPacket
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry as UpdaterEntry

class BreakpointsManager(val isClient: Boolean) {
    val registry = mutableMapOf<Identifier, BreakPointType>()
    private var currentBpId = 0
    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()

    fun register(type: BreakPointType) {
        if (registry.containsKey(type.id)) throw Exception("Duplicate BreakPointType ${type.id}")
        registry[type.id] = type
    }

    init {
        register(BlockUpdateOtherBreakpoint)
        register(BlockUpdatedBreakpoint)
        register(RedstoneMeterBreakpoint)

        // todo: debug only
        if (!isClient) {
            breakpointMap[0] = BlockUpdateOtherBreakpoint.create(0).apply {
                pos = BlockPos.ORIGIN
                options = BlockUpdateEvent.NC
                world = World.OVERWORLD.value
                handler.add(FreezeGame())
            }
            currentBpId++
        }
    }

    fun read(buf: PacketByteBuf): BreakPoint {
        val id = buf.readIdentifier()
        val bpId = buf.readVarInt()
        return registry[id]?.create(bpId)?.apply {
            read(buf)
        } ?: throw Exception("Unknown BreakPoint $id")
    }

    fun write(bp: BreakPoint, buf: PacketByteBuf) {
        buf.writeIdentifier(bp.type.id)
        buf.writeVarInt(bp.id)
        bp.write(buf)
    }

    fun sendAll(sender: PacketSender) {
        sender.sendPacket(SyncBreakpointsPacket(breakpointMap.values))
    }

    fun clear() {
        breakpointMap.clear()
    }

    fun <T : UpdaterEntry> checkBreakpointsForUpdating(stage: AbstractBlockUpdateStage<T>) {
        val worldId = stage.world?.registryKey?.value
        breakpointMap.values.asSequence()
            .filter { worldId == it.world }
            .forEach { it.call(stage) }
    }

    fun checkBreakpointsForScheduledTick() {

    }

    companion object {
        fun getBreakpointManager() = if (isClient) {
            MinecraftClient.getInstance().data().breakpoints
        } else {
            server.data.breakpoints
        }
    }
}
