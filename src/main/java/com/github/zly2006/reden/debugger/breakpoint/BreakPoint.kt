package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ADD
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import com.github.zly2006.reden.utils.server
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

interface BreakPointType {
    val id: Identifier
    val description: Text
    fun create(id: Int): BreakPoint
}

abstract class BreakPoint(
    val id: Int,
    open val type: BreakPointType
) {
    /**
     * Note: in this abstract class we only store world, pos is just something used like interface
     * we will not check pos in this class, check it in subclasses [call] method
     */
    abstract val pos: BlockPos?
    var world: Identifier? = null
    val serverWorld: ServerWorld?
        get() = world?.let { server.getWorld(RegistryKey.of(RegistryKeys.WORLD, it)) }

    /**
     * @see com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion
     */
    var flags = ADD or ENABLED
    open var handler: MutableList<BreakPointBehavior> = mutableListOf(); protected set
    open fun call(event: Any) {
        handler.sortBy { it.priority }
        handler.forEach { it.onBreakPoint(this, event) }
    }
    abstract fun write(buf: PacketByteBuf)
    abstract fun read(buf: PacketByteBuf)
}
