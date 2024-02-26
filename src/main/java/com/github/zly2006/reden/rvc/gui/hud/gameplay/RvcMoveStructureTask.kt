package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.malilib.RVC_CONFIRM_KEY
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.task.Task
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class RvcMoveStructureTask(
    private val world: World,
    val placingStructure: IStructure,
    id: String = "move_structure"
) : Task(id) {
    open var currentOrigin: BlockPos? = MinecraftClient.getInstance().player?.blockPos

    open fun customTexts(): List<Text> = listOf()

    init {
        RvcHudRenderer.supplierMap["move_machine_hud"] = {
            val list = mutableListOf<Text>()
            val mc = MinecraftClient.getInstance()
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine"))
            // structure name
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine.structure", placingStructure.name))
            // current origin
            list.add(
                Text.translatable(
                    "reden.widget.rvc.hud.move_machine.origin",
                    currentOrigin?.x,
                    currentOrigin?.y,
                    currentOrigin?.z
                )
            )
            list += customTexts()
            // how to confirm
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine.confirm", RVC_CONFIRM_KEY.stringValue))
            list
        }
    }

    override fun onCancel(): Boolean {
        currentOrigin = null // clear the area
        RvcHudRenderer.supplierMap -= "move_machine_hud"
        active = false
        return true
    }

    override fun onConfirm(): Boolean {
        val pos = currentOrigin ?: return false
        currentOrigin = null
        placingStructure.createPlacement(world, pos).paste()
        active = false
        return true
    }

    override fun onClientSideWorldChanged(newWorld: ClientWorld?) {
        onCancel()
    }
}
