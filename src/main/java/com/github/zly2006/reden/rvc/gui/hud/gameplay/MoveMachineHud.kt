package com.github.zly2006.reden.rvc.gui.hud.gameplay

import com.github.zly2006.reden.malilib.RVC_CONFIRM_KEY
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer
import com.github.zly2006.reden.utils.litematicaInstalled
import fi.dy.masa.litematica.world.SchematicWorldHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

private var placingStructure: IStructure? = null
private var currentOrigin: BlockPos? = null
    set(value) {
        if (field != null && placingStructure != null && litematicaInstalled) {
            SchematicWorldHandler.getSchematicWorld()?.apply {
                val placement = placingStructure!!.createPlacement(this, field!!)
                placement.clearArea()
            }
        }
        field = value
        if (field != null && placingStructure != null && litematicaInstalled) {
            SchematicWorldHandler.getSchematicWorld()?.apply {
                val placement = placingStructure!!.createPlacement(this, field!!)
                placement.paste()
            }
        }
    }

fun start(structure: IStructure, okCallback: (BlockPos) -> Unit) {
    placingStructure = structure
    RvcHudRenderer.supplierMap["move_machine_hud"] = {
        val list = mutableListOf<Text>()
        val mc = MinecraftClient.getInstance()
        if (placingStructure != null) {
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine"))
            // structure name
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine.structure", placingStructure!!.name))
            // current origin
            list.add(
                Text.translatable(
                    "reden.widget.rvc.hud.move_machine.origin",
                    currentOrigin?.x,
                    currentOrigin?.y,
                    currentOrigin?.z
                )
            )
            // litematica
            if (litematicaInstalled) {
                list.add(Text.translatable("reden.widget.rvc.hud.move_machine.litematica"))
            }
            // how to confirm
            list.add(Text.translatable("reden.widget.rvc.hud.move_machine.confirm", RVC_CONFIRM_KEY.stringValue))
        }
        list
    }
    currentOrigin = MinecraftClient.getInstance().player?.blockPos
}
