package com.github.zly2006.reden.access

import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.UNDO_CHEATING_ONLY
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.isSinglePlayerAndCheating
import net.minecraft.block.entity.BlockEntity
import net.minecraft.command.EntitySelector
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.TntEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
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
    var isRecording: Boolean = false
    var pearlListening: Boolean = false

    data class Entry(
        val blockState: NbtCompound,
        val blockEntityClazz: Class<BlockEntity>?,
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
        val entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        val data: MutableMap<Long, Entry> = mutableMapOf()
    ) {
        fun fromWorld(world: World, pos: BlockPos): Entry {
            val be = world.getBlockEntity(pos)
            return Entry(
                NbtHelper.fromBlockState(world.getBlockState(pos)),
                be?.javaClass,
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
                        this@UndoRedoRecord.entities.computeIfAbsent(entity.uuid) {
                            EntityEntryImpl(entity.type, NbtCompound().apply(entity::writeNbt), entity.blockPos)
                        }
                    }
                }
            }
        }

        open fun getMemorySize() = data.asSequence().map { it.value.getMemorySize() }.sum() +
                entities.map { 16 + (it.value.nbt.sizeInBytes ?: 0) }.sum()
    }
    class UndoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val cause: Cause = Cause.UNKNOWN
    ) : UndoRedoRecord(id, lastChangedTick, entities, data) {
        enum class Cause(message: Text) {
            BREAK_BLOCK(Text.translatable("reden.feature.undo.cause.break_block")),
            USE_BLOCK(Text.translatable("reden.feature.undo.cause.use_block")),
            USE_ITEM(Text.translatable("reden.feature.undo.cause.use_item")),
            ATTACK_ENTITY(Text.translatable("reden.feature.undo.cause.attack_entity")),
            COMMAND(Text.translatable("reden.feature.undo.cause.command")),
            LITEMATICA_TASK(Text.translatable("reden.feature.undo.cause.litematica_task")),
            UNKNOWN(Text.translatable("reden.feature.undo.cause.unknown"))
        }
    }
    class RedoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val undoRecord: UndoRecord
    ): UndoRedoRecord(id, lastChangedTick, entities, data) {
        override fun getMemorySize() = super.getMemorySize() + undoRecord.getMemorySize()
    }

    interface EntityEntry {
        val entity: EntityType<*>
        val nbt: NbtCompound
        val pos: BlockPos
    }

    class EntityEntryImpl(
        override val entity: EntityType<*>,
        override val nbt: NbtCompound,
        override val pos: BlockPos
    ): EntityEntry


    object NotExistEntityEntry: EntityEntry {
        override val entity: EntityType<*> = errorEntityType
        override val nbt: NbtCompound = NbtCompound()
        override val pos: BlockPos = BlockPos.ORIGIN
    }
}

private val errorEntityType =  EntityType<Entity>(
    null,
    SpawnGroup.AMBIENT,
    false,
    false,
    false,
    false,
    null,
    null,
    0,
    0,
    null
)