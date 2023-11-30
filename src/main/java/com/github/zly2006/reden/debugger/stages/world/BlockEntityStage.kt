package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.world.chunk.BlockEntityTickInvoker
import net.minecraft.world.chunk.WorldChunk

class BlockEntityStage(
    val _parent: BlockEntitiesRootStage,
    val ticker: BlockEntityTickInvoker?
): TickStage("block_entity", _parent), TickStageWithWorld {
    fun BlockEntityTickInvoker.type(): BlockEntityType<*>? = when (this) {
        is WorldChunk.DirectBlockEntityTickInvoker<*> -> blockEntity.type
        is WorldChunk.WrappedBlockEntityTickInvoker -> wrapped.type()
        else -> null
    }
    override val world get() = _parent.world
    var pos = ticker?.pos
    var type = ticker?.type()

    override val displayName = Text.translatable("reden.debugger.tick_stage.block_entity", pos?.toShortString(), type)

    override fun tick() {
        super.tick()

        _parent.world!!.data().blockEntityTickInvoker = ticker!!
        _parent.world!!.tickBlockEntities()
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        pos = buf.readBlockPos()
        type = Registries.BLOCK_ENTITY_TYPE.get(buf.readIdentifier())
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(pos!!)
        buf.writeIdentifier(BlockEntityType.getId(type!!))
    }
}
