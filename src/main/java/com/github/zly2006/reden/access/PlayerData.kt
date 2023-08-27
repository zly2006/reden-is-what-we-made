package com.github.zly2006.reden.access

import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.UNDO_CHEATING_ONLY
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.isSinglePlayerAndCheating
import net.minecraft.command.EntitySelector
import net.minecraft.entity.EntityType
import net.minecraft.entity.TntEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*


class PlayerData(
    val player: ServerPlayerEntity,
) {
    val canRecord: Boolean
        get() = if (!isClient) {
            RedenCarpetSettings.allowedUndoSizeInBytes != 0
        } else if (UNDO_CHEATING_ONLY.booleanValue) {
            true
        } else isSinglePlayerAndCheating
    val undo: MutableList<UndoRecord> = mutableListOf()
    val redo: MutableList<RedoRecord> = mutableListOf()
    var undoUsedBytes: Int = 0
    var isRecording: Boolean = false
    var pearlListening: Boolean = false

    fun stopRecording(world: World) {
        UpdateMonitorHelper.playerStopRecording(player)
    }

    data class Entry(
        val blockState: NbtCompound,
        val blockEntity: NbtCompound?,
    ) {

        fun getMemorySize() =
            blockState.sizeInBytes + (blockEntity?.sizeInBytes ?: 0)
    }
    internal interface PlayerDataAccess {
        fun getRedenPlayerData(): PlayerData
    }

    companion object {
        fun ServerPlayerEntity.data(): PlayerData {
            return (this as PlayerDataAccess).getRedenPlayerData()
        }
    }

    open class UndoRedoRecord(
        val id: Long,
        var lastChangedTick: Int = 0,
        val entities: MutableMap<UUID, EntityEntry?> = mutableMapOf(),
        val data: MutableMap<Long, Entry> = mutableMapOf()
    ) {
        fun fromWorld(world: World, pos: BlockPos): Entry {
            return Entry(
                NbtHelper.fromBlockState(world.getBlockState(pos)),
                world.getBlockEntity(pos)?.createNbt()
            ).apply {
                if (world.getBlockState(pos).getCollisionShape(world, pos).boundingBoxes.size != 0) {
                    val list = world.getEntitiesByType(
                        EntitySelector.PASSTHROUGH_FILTER,
                        world.getBlockState(pos).getCollisionShape(world, pos).boundingBox
                            .offset(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                            .expand(0.1),
                    ) { x -> x !is PlayerEntity && x !is TntEntity }
                    list.forEach { entity ->
                        synchronized(this@UndoRedoRecord) {
                            this@UndoRedoRecord.entities.computeIfAbsent(entity.uuid) {
                                EntityEntry(entity.type, NbtCompound().apply(entity::writeNbt), entity.blockPos)
                            }
                        }
                    }
                }
            }
        }

        open fun getMemorySize() = data.asSequence().map { it.value.getMemorySize() }.sum() +
                entities.map { 16 + (it.value?.nbt?.sizeInBytes ?: 0) }.sum()
    }
    class UndoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry?> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf()
    ) : UndoRedoRecord(id, lastChangedTick, entities, data)
    class RedoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry?> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val undoRecord: UndoRecord
    ): UndoRedoRecord(id, lastChangedTick, entities, data) {
        override fun getMemorySize() = super.getMemorySize() + undoRecord.getMemorySize()
    }

    class EntityEntry(
        val entity: EntityType<*>,
        val nbt: NbtCompound,
        val pos: BlockPos
    )
}
