package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.utils.litematicaInstalled
import com.github.zly2006.reden.utils.redenError
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.world.SchematicWorldHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Note: Hey i know this class needs to be rewritten for litematica compatibility
 * but is too hard to do it, litematica does not have a highly abstracted API e.g.
 * [IStructure]
 */
class RvcMoveStructureLitematicaTask(
    world: World, placingStructure: IStructure
) : RvcMoveStructureTask(world, placingStructure, "move_structure_litematica") {
    companion object {
        init {
            if (!litematicaInstalled) {
                redenError("Litematica is not installed, cannot use this task: " + this::class.qualifiedName)
            }
        }
    }

    override fun customTexts() = listOf(
        Text.literal("Using litematica rendering.")
    )

    private val schematicWorld = SchematicWorldHandler.getSchematicWorld() ?: error("Failed to load litematica world")
    override var currentOrigin: BlockPos? = MinecraftClient.getInstance().player?.blockPos
        set(value) {
            placementSchematicWorld?.clearArea()
            placementSchematicWorld = null
            field = value
            if (value != null) {
                placementSchematicWorld = placingStructure.createPlacement(schematicWorld, value)
                placementSchematicWorld?.paste()
            }
        }
    private var placementSchematicWorld: IPlacement? = placingStructure.createPlacement(schematicWorld, currentOrigin!!)

    override fun onCancel(): Boolean {
        placementSchematicWorld?.clearArea()
        return super.onCancel()
    }

    override fun onStopped() {
        super.onStopped()
        val manager = DataManager.getSchematicPlacementManager()
        // refresh litematica
        manager.allSchematicsPlacements.forEach(manager::markChunksForRebuild)
    }
}
