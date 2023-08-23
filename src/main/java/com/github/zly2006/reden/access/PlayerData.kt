package com.github.zly2006.reden.access

import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.UNDO_CHEATING_ONLY
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.utils.isClient
import net.minecraft.client.MinecraftClient
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
    private infix fun Boolean?.and(other: Boolean?) = this ?: false && other ?: false
    val canRecord: Boolean
        get() = (!isClient && RedenCarpetSettings.allowedUndoSizeInBytes == 0) ||
                (UNDO_CHEATING_ONLY.booleanValue && MinecraftClient.getInstance()?.let {
                    (it.server?.isSingleplayer and it.player?.hasPermissionLevel(2))
                } == true)
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
        val entities: MutableMap<UUID, EntityEntry> = hashMapOf()
    ) {
        class EntityEntry(
            val entity: EntityType<*>,
            val nbt: NbtCompound,
            val pos: BlockPos
        )

        fun getMemorySize() =
            blockState.sizeInBytes + (blockEntity?.sizeInBytes ?: 0) +
                    entities.map { it.value.nbt.sizeInBytes }.sum()

        companion object {
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
                        list.forEach {
                            entities[it.uuid] = EntityEntry(it.type, NbtCompound().apply(it::writeNbt), it.blockPos)
                        }
                    }
                }
            }
        }
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
        val entities: MutableMap<UUID, Entry.EntityEntry?> = hashMapOf(),
        val data: MutableMap<Long, Entry> = hashMapOf()
    ) {
        open fun getMemorySize() = data.asSequence().map { it.value.getMemorySize() }.sum() +
                entities.map { 16 + (it.value?.nbt?.sizeInBytes ?: 0) }.sum()
    }
    class UndoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, Entry.EntityEntry?> = hashMapOf(),
        data: MutableMap<Long, Entry> = hashMapOf()
    ) : UndoRedoRecord(id, lastChangedTick, entities, data)
    class RedoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, Entry.EntityEntry?> = hashMapOf(),
        data: MutableMap<Long, Entry> = hashMapOf(),
        val undoRecord: UndoRecord
    ): UndoRedoRecord(id, lastChangedTick, entities, data) {
        override fun getMemorySize() = super.getMemorySize() + undoRecord.getMemorySize()
    }
}
