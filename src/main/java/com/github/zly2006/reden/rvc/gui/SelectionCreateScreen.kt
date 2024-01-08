package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text

private val DEFAULT_STATUS_TIP = Text.literal("Create a new RVC repository")

class SelectionCreateScreen: BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!
    private val confirmNameButton: ButtonComponent = Components.button(Text.literal("OK")) {
        val repository = RvcRepository.create(nameField.text, client!!.getWorldInfo())
        client!!.data.rvcStructures[nameField.text] = repository
        selectedRepository = repository
        client!!.data.mc.setScreen(null)
    }.active(false)
    private val nameField = Components.textBox(Sizing.fixed(100)).apply {
        onChanged().subscribe {
            if (it in client!!.data.rvcStructures) {
                label.text(Text.literal("Name already exists").red())
                confirmNameButton.active(false)
            } else if (it.isEmpty()) {
                label.text(Text.literal("Name cannot be empty").red())
                confirmNameButton.active(false)
            } else {
                label.text(DEFAULT_STATUS_TIP)
                confirmNameButton.active(true)
            }
        }
    }
    private val label = Components.label(DEFAULT_STATUS_TIP)

    override fun build(rootComponent: FlowLayout) {
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)

        rootComponent.child(
            Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.literal("Name:")))
                .child(nameField)
                .child(confirmNameButton)
                .verticalAlignment(VerticalAlignment.CENTER)
        )
        rootComponent.child(label)
    }
}
