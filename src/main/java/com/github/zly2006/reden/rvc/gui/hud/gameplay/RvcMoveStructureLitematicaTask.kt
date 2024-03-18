package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo
import com.github.zly2006.reden.task.Task
import com.github.zly2006.reden.task.taskStack
import com.github.zly2006.reden.utils.litematicaInstalled
import com.github.zly2006.reden.utils.redenError
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.world.SchematicWorldHandler
import fi.dy.masa.malilib.util.IntBoundingBox
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.WorldChunk

class RvcMoveStructureLitematicaTask(
    world: WorldInfo, placingStructure: IStructure, successCallback: (Task) -> Unit = {}
) : RvcMoveStructureTask(world, placingStructure, "move_structure_litematica", successCallback) {
    companion object {
        @JvmStatic
        fun stackTop() = taskStack.lastOrNull() as? RvcMoveStructureLitematicaTask?

        init {
            if (!litematicaInstalled) {
                redenError("Litematica is not installed, cannot use this task: " + this::class.qualifiedName)
            }
        }
    }

    override fun customTexts() = listOf(
        Text.literal("Using litematica rendering.")
    )

    override fun onCreated() {
        super.onCreated()
        placementSchematicWorld?.startMoving()
        currentOrigin = MinecraftClient.getInstance().player?.blockPos
    }

    private val schematicWorld =
        SchematicWorldHandler.getSchematicWorld() ?: redenError("Failed to load litematica world")
    override var currentOrigin: BlockPos? = null
        set(value) {
            placementSchematicWorld?.clearArea()
            placementSchematicWorld?.blockBox()?.streamChunkPos()
                ?.forEach(DataManager.getSchematicPlacementManager()::markChunkForRebuild)

            placementSchematicWorld = null
            field = value
            if (value != null) {
                placementSchematicWorld = placingStructure
                    .createPlacement(schematicWorld, value)
                    .also(IPlacement::paste)
                this.box = placementSchematicWorld!!.blockBox().apply {
                    this.streamChunkPos().forEach(DataManager.getSchematicPlacementManager()::markChunkForRebuild)
                }.toMasaBox()
            }
        }
    var box: IntBoundingBox? = null
    private var placementSchematicWorld: IPlacement? = null

    override fun onStopped() {
        try {
            super.onStopped()
            // refresh litematica
            placementSchematicWorld?.clearArea()
            placementSchematicWorld?.blockBox()?.streamChunkPos()
                ?.forEach(DataManager.getSchematicPlacementManager()::markChunkForRebuild)
        } catch (e: Exception) {
            Reden.LOGGER.error("Failed to stop moving structure", e)
            active = false
        }
    }

    fun previewContainsChunk(chunkPos: ChunkPos) =
        placementSchematicWorld?.blockBox()?.streamChunkPos()?.anyMatch { it == chunkPos } ?: false

    fun pasteChunk(chunk: WorldChunk) {
        // fixme: perf: paste just one chunk
        try {
            placingStructure.createPlacement(chunk.world, currentOrigin ?: return).paste()
        } catch (e: Exception) {
            Reden.LOGGER.error("Failed to paste chunk ${chunk.pos} at ${chunk.world.registryKey.value}", e)
            active = false
        }
    }
}

private fun BlockBox.toMasaBox() = IntBoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
