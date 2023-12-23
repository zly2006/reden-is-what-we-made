package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.debugger.breakpoint.behavior.StatisticsBehavior
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.network.SyncBreakpointsPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry as UpdaterEntry

class BreakpointsManager(val isClient: Boolean) {
    val registry = mutableMapOf<Identifier, BreakPointType>()
    val behaviorRegistry = mutableMapOf<Identifier, BreakPointBehavior>()
    private var currentBpId = 0
    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()

    fun register(type: BreakPointType) {
        if (registry.containsKey(type.id)) error("Duplicate BreakPointType ${type.id}")
        registry[type.id] = type
    }
    fun register(behavior: BreakPointBehavior) {
        if (behaviorRegistry.containsKey(behavior.id)) error("Duplicate BreakPointBehaviorType ${behavior.id}")
        behaviorRegistry[behavior.id] = behavior
    }

    init {
        register(BlockUpdateOtherBreakpoint)
        register(BlockUpdatedBreakpoint)
        register(RedstoneMeterBreakpoint)

        register(FreezeGame())
        register(StatisticsBehavior())

        // todo: debug only
        if (!isClient) {
            breakpointMap[0] = BlockUpdateOtherBreakpoint.create(0).apply {
                pos = BlockPos.ORIGIN
                options = BlockUpdateEvent.NC
                world = World.OVERWORLD.value
                name = "Test"
                handler.add(BreakPoint.Handler(FreezeGame(), name = "Test Handler"))
            }
            currentBpId++
        }
    }

    fun read(buf: PacketByteBuf): BreakPoint {
        val id = buf.readIdentifier()
        val bpId = buf.readVarInt()
        return registry[id]?.create(bpId)?.apply {
            name = buf.readString()
            world = buf.readIdentifier()
            val handlerSize = buf.readVarInt()
            repeat(handlerSize) {
                handler.add(
                    BreakPoint.Handler(
                        behaviorRegistry[buf.readIdentifier()] ?: error("Unknown behavior type: $id"),
                        buf.readVarInt(),
                        buf.readString()
                    )
                )
            }
            read(buf)
        } ?: throw Exception("Unknown BreakPoint $id")
    }

    fun write(buf: PacketByteBuf, bp: BreakPoint) {
        buf.writeIdentifier(bp.type.id)
        buf.writeVarInt(bp.id)
        buf.writeString(bp.name)
        buf.writeIdentifier(bp.world!!)
        buf.writeVarInt(bp.handler.size)
        bp.handler.forEach {
            buf.writeIdentifier(it.type.id)
            buf.writeVarInt(it.priority)
            buf.writeString(it.name)
        }
        bp.write(buf)
    }

    fun sendAll(sender: PacketSender) {
        sender.sendPacket(SyncBreakpointsPacket(breakpointMap.values))
    }

    fun clear() {
        breakpointMap.clear()
    }

    fun <T : UpdaterEntry> checkBreakpointsForUpdating(stage: AbstractBlockUpdateStage<T>) {
        breakpointMap.values.asSequence()
            .filter { it.flags and ENABLED != 0 }
            .filter { stage.world == it.serverWorld }
            .forEach { it.call(stage) }
    }

    fun checkBreakpointsForScheduledTick() {

    }

    fun sync(breakpoint: BreakPoint) {
        if (isClient) {
            ClientPlayNetworking.send(UpdateBreakpointPacket(
                breakpoint,
                flag = UpdateBreakpointPacket.UPDATE or breakpoint.flags,
                bpId = breakpoint.id
            ))
        } else {
            server.sendToAll(UpdateBreakpointPacket(
                breakpoint,
                flag = UpdateBreakpointPacket.UPDATE or breakpoint.flags,
                bpId = breakpoint.id
            ))
        }
    }

    companion object {
        fun getBreakpointManager() = if (isClient) {
            MinecraftClient.getInstance().data().breakpoints
        } else {
            server.data.breakpoints
        }
    }
}
