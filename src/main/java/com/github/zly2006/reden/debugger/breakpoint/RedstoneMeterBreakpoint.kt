package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import com.github.zly2006.reden.utils.codec.BlockPosSerializer
import com.github.zly2006.reden.utils.codec.IdentifierSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrDefault

/**
 * todo: implement something just like the Redstone Multimeter mod
 * [Redstone Multimeter](https://www.curseforge.com/minecraft/mc-mods/redstone-multimeter)
 */
@Serializable
class RedstoneMeterBreakpoint(
    override val id: Int
) : BreakPoint {
    override val type get() = Companion
    override var name: String = ""
    @Serializable(with = BlockPosSerializer::class)
    override lateinit var pos: BlockPos
    @Serializable(with = IdentifierSerializer::class)
    override var world: Identifier? = null
    override var flags: Int = ENABLED
    override val handler: MutableList<BreakPoint.Handler> = mutableListOf()

    companion object: BreakPointType {
        val POWERED = Properties.POWERED!!

        fun isPowered(state: BlockState): Boolean {
            return state.getOrEmpty(POWERED).getOrDefault(false)
        }

        override val id: Identifier = Reden.identifier("redstone_meter")
        override val description: Text = Text.literal("RedstoneMeter")
        override fun create(id: Int) = RedstoneMeterBreakpoint(id)
        override fun kSerializer(): KSerializer<out BreakPoint> {
            return serializer()
        }
    }
}
