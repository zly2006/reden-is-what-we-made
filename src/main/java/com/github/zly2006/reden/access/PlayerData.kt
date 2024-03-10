package com.github.zly2006.reden.access

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.UNDO_CHEATING_ONLY
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.isSinglePlayerAndCheating
import com.github.zly2006.reden.utils.redenError
import com.github.zly2006.reden.utils.server
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.command.EntitySelector
import net.minecraft.entity.EntityType
import net.minecraft.entity.TntEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class PlayerData(
    val player: ServerPlayerEntity,
) {
    fun topRedo() {
        player.sendMessage(Text.of(redo.lastOrNull {
            it.data.isNotEmpty() && it.entities.isNotEmpty()
        }?.toString()))
    }

    fun topUndo() {
        player.sendMessage(Text.of(undo.lastOrNull {
            it.data.isNotEmpty() && it.entities.isNotEmpty()
        }?.toString()))
    }

    val canRecord: Boolean
        get() = if (!isClient) {
            RedenCarpetSettings.Options.allowedUndoSizeInBytes != 0
        } else if (UNDO_CHEATING_ONLY.booleanValue) {
            true
        } else isSinglePlayerAndCheating
    val undo: MutableList<UndoRecord> = mutableListOf()
    val redo: MutableList<RedoRecord> = mutableListOf()
    var isRecording: Boolean = false
    var pearlListening: Boolean = false

    data class Entry(
        val state: BlockState,
        val blockEntity: NbtCompound?,
        val time: Int
    ) {
        fun getMemorySize() = (blockEntity?.sizeInBytes ?: 0) + 20
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
        override fun toString(): String {
            return """
+ UndoRedoRecord id=$id
+ size = ${getMemorySize()}
+ entities:
${entities.map { "${it.key} = ${it.value}" }.joinToString("\n")}
+ blocks:
${data.map { "${BlockPos.fromLong(it.key).toShortString()} = ${it.value.state}" }.joinToString("\n")}
            """.trimIndent()
        }

        fun fromWorld(world: World, pos: BlockPos, putNearByEntities: Boolean): Entry {
            val be = world.getBlockEntity(pos)
            val state = world.getBlockState(pos)
            if (isClient && MinecraftClient.getInstance().server?.isOnThread == false) {
                redenError("Cannot call undo stuff off main thread.")
            }
            return Entry(state, be?.lastSavedNbt(), server.ticks).apply {
                if (state.hasBlockEntity() && blockEntity == null) {
                    Reden.LOGGER.error("BlockEntity $be at $pos has no last saved nbt")
                }
                if (putNearByEntities &&
                    world.getBlockState(pos).getCollisionShape(world, pos).boundingBoxes.size != 0
                ) {
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
                data.size * 16 +
                entities.map { 16 + it.value.nbt.sizeInBytes }.sum()
    }

    class UndoRecord(
        id: Long,
        lastChangedTick: Int = 0,
        entities: MutableMap<UUID, EntityEntry> = mutableMapOf(),
        data: MutableMap<Long, Entry> = mutableMapOf(),
        val cause: Cause = Cause.UNKNOWN
    ) : UndoRedoRecord(id, lastChangedTick, entities, data) {
        var notified = false

        enum class Cause(val message: Text) {
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
        val nbt: NbtCompound
        val pos: BlockPos
    }

    class EntityEntryImpl(
        override val entity: EntityType<*>,
        override val nbt: NbtCompound,
        override val pos: BlockPos
    ) : EntityEntry {
        override fun toString() = "EntityEntryImpl(entity=$entity, nbt={${nbt.size} items}, pos=$pos)"
    }

    data object NotExistEntityEntry : EntityEntry {
        override val entity = null
        override val nbt: NbtCompound = NbtCompound()
        override val pos: BlockPos = BlockPos.ORIGIN
    }
}

private fun BlockEntity.lastSavedNbt() = (this as BlockEntityInterface).lastSavedNbt
