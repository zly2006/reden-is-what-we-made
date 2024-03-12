package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.malilib.RVC_CONFIRM_KEY
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.task.Task
import com.github.zly2006.reden.utils.server
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class RvcMoveStructureTask(
    private val world: World,
    val placingStructure: IStructure,
    id: String = "move_structure",
    val successCallback: (Task) -> Unit = {}
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
                    currentOrigin?.toShortString()
                )
            )
            list += customTexts()
            // how to confirm
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine.confirm", RVC_CONFIRM_KEY.stringValue))
            list
        }
    }

    override fun onStopped() {
        super.onStopped()
        currentOrigin = null // clear the area
        RvcHudRenderer.supplierMap -= "move_machine_hud"
    }

    override fun onConfirm(): Boolean {
        val pos = currentOrigin ?: return false
        currentOrigin = null
        placingStructure.createPlacement(world, pos).apply {
            if (placingStructure is TrackedStructure) {
                // todo multiple player
                GlobalScope.launch(server.asCoroutineDispatcher()) {
                    placingStructure.networkWorker!!.startUndoRecord(PlayerData.UndoRecord.Cause.RVC_MOVE)
                    placingStructure.networkWorker!!.paste()
                    placingStructure.networkWorker!!.stopUndoRecord()
                    placingStructure.networkWorker!!.execute {
                        placingStructure.setPlaced()
                        successCallback(this@RvcMoveStructureTask)
                    }
                }
            }
            else {
                paste()
                successCallback(this@RvcMoveStructureTask)
            }
        }
        active = false
        return true
    }
}
