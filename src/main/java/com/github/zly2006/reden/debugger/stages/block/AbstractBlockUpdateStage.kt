package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.storage.BlocksResetStorage
import com.github.zly2006.reden.network.TagBlockPos
import com.github.zly2006.reden.render.BlockBorder
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

abstract class AbstractBlockUpdateStage<T: Updater119.Entry>(
    name: String,
    parent: TickStage
): TickStage(name, parent), TickStageWithWorld {
    override val world get() = (parent as TickStageWithWorld).world
    abstract val entry: T
    val resetStorage = BlocksResetStorage()

    override fun preTick() {
        super.preTick()
        world!!.server.data.breakpoints.checkBreakpointsForUpdating(this)
    }

    override fun postTick() {
        super.postTick()
        world!!.server.data.breakpoints.checkBreakpointsForUpdating(this)
    }

    override fun reset() {
        if (world == null) {
            error("World is null, are you ticking this stage at a client?")
        }
        resetStorage.apply(world!!)
    }

    abstract val sourcePos: BlockPos
    abstract val targetPos: BlockPos?
    abstract val sourceBlock: Block
    override val displayName: MutableText
        get() = Text.translatable("reden.debugger.tick_stage.$name")
            .append(" ")
            .append(sourcePos.toShortString())
            .append(" -> ")
            .append(targetPos?.toShortString() ?: "null")
    override val description: MutableText
        get() = Text.translatable("reden.debugger.tick_stage.$name.desc")
            .append("\n")
            .append("Source Block: ")
            .append(sourceBlock.name)

    override fun focused(mc: MinecraftClient) {
        super.focused(mc)
        BlockBorder[sourcePos] = TagBlockPos.green
        if (targetPos != null) {
            BlockBorder[targetPos!!] = TagBlockPos.red
        }
    }

    override fun unfocused(mc: MinecraftClient) {
        super.unfocused(mc)
        BlockBorder.tags.remove(sourcePos.asLong())
        if (targetPos != null) BlockBorder.tags.remove(targetPos!!.asLong())
    }
}
