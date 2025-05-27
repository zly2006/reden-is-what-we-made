package com.github.zly2006.reden.access

import com.github.zly2006.reden.utils.multiver.Text
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.PrimedTnt
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class PlayerData(
    val player: ServerPlayer,
) {
    fun topRedo() {
        player.sendSystemMessage(Text.of(redo.lastOrNull {
            it.data.isNotEmpty() && it.entities.isNotEmpty()
        }?.toString()))
    }

    fun topUndo() {
        player.sendSystemMessage(Text.of(undo.lastOrNull {
            it.data.isNotEmpty() && it.entities.isNotEmpty()
        }?.toString()))
    }

    var behalfBy: ServerPlayer? = null
    val canRecord: Boolean = player.isCreative
    val undo: MutableList<UndoRecord> = mutableListOf()
    val redo: MutableList<RedoRecord> = mutableListOf()
    var isRecording: Boolean = false
    var pearlListening: Boolean = false

    data class Entry(
        val state: BlockState,
        val blockEntity: CompoundTag?,
        val time: Int
    ) {
        fun getMemorySize() = (blockEntity?.sizeInBytes() ?: 0) + 20
    }

    internal interface PlayerDataAccess {
        fun getRedenPlayerData(): PlayerData
    }

    companion object {
        fun ServerPlayer.data(): PlayerData {
            return (this as PlayerDataAccess).getRedenPlayerData()
        }
    }

    open class UndoRedoRecord(
        val id: Long,
        var lastChangedTick: Int = 0,
        val entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        val data: MutableMap<Long, Entry> = mutableMapOf()
    ) {
        override fun toString(): String {
            return """
+ UndoRedoRecord id=$id
+ size = ${getMemorySize()}
+ entities:
${entities.map { "${it.key} = ${it.value}" }.joinToString("\n")}
+ blocks:
${data.map { "${BlockPos.of(it.key).toShortString()} = ${it.value.state}" }.joinToString("\n")}
            """.trimIndent()
        }

        fun fromWorld(world: ServerLevel, pos: BlockPos, putNearByEntities: Boolean): Entry {
            val be = world.getBlockEntity(pos)
            val state = world.getBlockState(pos)
            return Entry(state, be?.lastSavedNbt(), world.server.tickCount).apply {
                if (state.hasBlockEntity() && blockEntity == null) {
//                    Reden.LOGGER.error("BlockEntity $be at $pos has no last saved nbt")
                }
                if (putNearByEntities &&
                    world.getBlockState(pos).getCollisionShape(world, pos).toAabbs().isNotEmpty()
                ) {
                    val list = world.getEntities(
                        EntitySelector.ANY_TYPE,
                        world.getBlockState(pos).getCollisionShape(world, pos).bounds()
                            .move(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                            .inflate(0.1),
                    ) { x -> x !is ServerPlayer && x !is PrimedTnt }
                    list.forEach { entity ->
                        this@UndoRedoRecord.entities.computeIfAbsent(entity.uuid) {
                            EntityEntryImpl(entity.type, CompoundTag().apply(entity::save), entity.blockPosition())
                        }
                    }
                }
            }
        }

        open fun getMemorySize() = data.asSequence().map { it.value.getMemorySize() }.sum() +
                data.size * 16 +
                entities.map { 16 + it.value.nbt.sizeInBytes() }.sum()
    }

    class UndoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val cause: Cause = Cause.UNKNOWN
    ) : UndoRedoRecord(id, lastChangedTick, entities, data) {
        var notified = false

        enum class Cause(val message: Component) {
            BREAK_BLOCK(Text.translatable("reden.feature.undo.cause.break_block")),
            USE_BLOCK(Text.translatable("reden.feature.undo.cause.use_block")),
            USE_ITEM(Text.translatable("reden.feature.undo.cause.use_item")),
            USE_ENTITY(Text.translatable("reden.feature.undo.cause.use_entity")),
            ATTACK_ENTITY(Text.translatable("reden.feature.undo.cause.attack_entity")),
            COMMAND(Text.translatable("reden.feature.undo.cause.command")),
            LITEMATICA_TASK(Text.translatable("reden.feature.undo.cause.litematica_task")),
            PROJECTILE(Text.translatable("reden.feature.undo.cause.projectile")),
            RVC_MOVE(Text.translatable("reden.feature.undo.cause.rvc_move")),
            RVC_RESTORE(Text.translatable("reden.feature.undo.cause.rvc_restore")),
            RVC_CHECKOUT(Text.translatable("reden.feature.undo.cause.rvc_checkout")),
            UNKNOWN(Text.translatable("reden.feature.undo.cause.unknown"))
        }
    }

    class RedoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val undoRecord: UndoRecord
    ) : UndoRedoRecord(id, lastChangedTick, entities, data) {
        override fun getMemorySize() = super.getMemorySize() + undoRecord.getMemorySize()
    }

    interface EntityEntry {
        val entity: EntityType<*>?
        val nbt: CompoundTag
        val pos: BlockPos
    }

    class EntityEntryImpl(
        override val entity: EntityType<*>,
        override val nbt: CompoundTag,
        override val pos: BlockPos
    ) : EntityEntry {
        override fun toString() = "EntityEntryImpl(entity=$entity, nbt={${nbt.size()} items}, pos=$pos)"
    }

    data object NotExistEntityEntry : EntityEntry {
        override val entity = null
        override val nbt: CompoundTag = CompoundTag()
        override val pos: BlockPos = BlockPos.ZERO
    }
}

private fun BlockEntity.lastSavedNbt() = (this as BlockEntityInterface).lastSavedNbt
