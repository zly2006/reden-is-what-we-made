package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import net.minecraft.block.BlockState
import net.minecraft.network.PacketByteBuf
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrDefault

/**
 * todo: implement something just like the Redstone Multimeter mod
 * [Redstone Multimeter](https://www.curseforge.com/minecraft/mc-mods/redstone-multimeter)
 */
class RedstoneMeterBreakpoint(id: Int) : BreakPoint(id, Companion) {
    lateinit var pos: BlockPos
    companion object: BreakPointType {
        val POWERED = Properties.POWERED!!

        fun isPowered(state: BlockState): Boolean {
            return state.getOrEmpty(POWERED).getOrDefault(false)
        }

        override val id: Identifier = Reden.identifier("redstone_meter")
        override val description: Text = Text.literal("RedstoneMeter")
        override fun create(id: Int) = RedstoneMeterBreakpoint(id)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun read(buf: PacketByteBuf) {
        pos = buf.readBlockPos()
    }
}
