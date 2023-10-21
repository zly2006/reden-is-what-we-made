package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.StageTree
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import org.lwjgl.glfw.GLFW

class UpdateStackLayout(val tree: StageTree): FlowLayout(Sizing.content(), Sizing.content(), Algorithm.VERTICAL) {
    private fun element(stage: TickStage): Component {
        val row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(16))
        row.child(Components.item(ItemStack(Items.ITEM_FRAME)))
        row.child(Components.label(stage.displayName))

        row.mouseDown().subscribe { _, _, button ->
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                setDescriptionInfo.forEach { it(stage) }
                true
            } else {
                false
            }
        }
        return row
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        context.fill(
            0,
            0,
            determineHorizontalContentSize(null),
            determineVerticalContentSize(null),
            0x80_00_00_00.toInt()
        )
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    init {
        update()
    }

    fun update() {
        clearChildren()

        var node = tree.child
        do {
            child(element(node!!.stage))
            node = node.parent
        } while (node != tree.root)
    }
}
