package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.isClient
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf

class EntityStage(
    val _parent: EntitiesRootStage,
    var entity: Entity?,
    var entityId: Int
): TickStage("entity", _parent) {
    constructor(_parent: EntitiesRootStage, entity: Entity?) :
            this(_parent, entity, entity?.id ?: -1)

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
            _parent.world.getEntityById(entityId)
        }
    }
}
