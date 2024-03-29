package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.utils.isClient
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class EntityStage(
    val _parent: EntitiesRootStage,
    var entity: Entity?,
    var entityId: Int
): TickStage("entity", _parent), TickStageWithWorld {
    override val world get() = _parent.world

    constructor(_parent: EntitiesRootStage, entity: Entity?) :
            this(_parent, entity, entity?.id ?: -1)

    override val displayName = Text.translatable("reden.debugger.tick_stage.entity", entityId, entity?.displayName)

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeInt(entityId)
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        entityId = buf.readInt()
        entity = if (isClient) {
            val mc = MinecraftClient.getInstance()
            mc.world?.getEntityById(entityId)
        } else {
            _parent.world!!.getEntityById(entityId)
        }
    }
}
