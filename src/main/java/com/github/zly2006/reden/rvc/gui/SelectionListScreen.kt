package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

val selectedStructure get() = selectedRepository?.head()
var selectedRepository: RvcRepository? = null

class SelectionListScreen: BaseOwoScreen<FlowLayout>() {
    var selectedUIElement: RepositoryLine? = null
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    inner class RepositoryLine(
        private val repository: RvcRepository
    ): FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        val select: CheckboxComponent = Components.checkbox(Text.empty()).apply {
            checked(selectedRepository == repository)
            onChanged {
                if (it) {
                    selectedUIElement?.select?.checked(false)

                    selectedRepository = repository
                    selectedUIElement = this@RepositoryLine
                } else {
                    selectedRepository = null
                    selectedUIElement = null
                }
            }
        }
        val left = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        val right = Containers.horizontalFlow(Sizing.content(), Sizing.content())

        init {
            gap(5)
            left.gap(5).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            right.gap(5).alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
            child(left)
            horizontalAlignment(HorizontalAlignment.CENTER)
            child(right)

            left.child(select)
            left.child(Components.label(Text.literal(repository.name)))
            right.child(Components.button(Text.literal("Delete")) {
                onFunctionUsed("delete_rvcStructure")
            })
            right.child(Components.button(Text.literal("Open")) {
                onFunctionUsed("open_rvcStructure")
                MinecraftClient.getInstance().setScreen(SelectionInfoScreen(repository.head()))
            }.apply {
                active(false)
            })
        }
    }

    private var infoBox: Component? = null

    override fun build(rootComponent: FlowLayout) {
        val mc = MinecraftClient.getInstance()
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(Components.button(Text.literal("New")) {
            onFunctionUsed("new_rvcListScreen")
            client!!.setScreen(SelectionCreateScreen())
        })

        mc.data.rvcStructures.values.forEach {
            rootComponent.child(RepositoryLine(it))
        }

        infoBox = Containers.verticalScroll(
            Sizing.fill(100),
            Sizing.fill(40),
            Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply {
                fun childTr(key: String, vararg args: Any) = child(Components.label(Text.translatable(key, *args)))
                selectedStructure?.run {
                    childTr("reden.widget.rvc.structure.name", name)
                    childTr("reden.widget.rvc.structure.block_count", blocks.count())
                    childTr("reden.widget.rvc.structure.entity_count", entities.count())
                    if (fluidScheduledTicks.isNotEmpty() || blockScheduledTicks.isNotEmpty() || blockEvents.isNotEmpty()) {
                        childTr("reden.widget.rvc.structure.scheduled_tick_unstable")
                    }
                }
            }
        )
        rootComponent.child(Components.label(Text.empty()))
    }
}
