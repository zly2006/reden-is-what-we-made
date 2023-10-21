package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.rvc.io.LitematicaIO
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.litematicaInstalled
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import java.nio.file.Path

internal var onSelectedChanged = mutableListOf<(TrackedStructure?) -> Unit>()
val trackedStructureList = mutableListOf<TrackedStructure>(
    TrackedStructure("Test1"),
    TrackedStructure("Test2"),
    TrackedStructure("Test3"),
    TrackedStructure("Test4"),
    TrackedStructure("Test5"),
    TrackedStructure("Test6"),
    TrackedStructure("Test7"),
    TrackedStructure("Test8"),
    TrackedStructure("Test9"),
    TrackedStructure("Test10"),
    TrackedStructure("Test11"),
    TrackedStructure("Test12"),
)

var selectedStructure: TrackedStructure? = null; internal set

fun onSyncSelection() {

}

fun syncSelectionLocal() {

}

class SelectionListScreen: BaseOwoScreen<FlowLayout>() {
    var changeListener: ((TrackedStructure?) -> Unit)? = null
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!
    init {
        val mc = MinecraftClient.getInstance()
        trackedStructureList.forEach { it.world = mc.world!! }
    }

    override fun removed() {
        super.removed()
        onSelectedChanged.remove(changeListener)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(
            Components.button(
                Text.literal("A Button")
            ) { println("click") }
        )
        fun selectionGrid(): Component {
            val grid = Containers.grid(
                Sizing.content(), Sizing.content(),
                trackedStructureList.size,
                3
            )
            grid.allowOverflow(true).alignment(
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP
            )
            trackedStructureList.forEachIndexed { index, data ->
                val checkBox = Components.checkbox(Text.empty())
                checkBox.checked(data == selectedStructure)
                checkBox.onChanged {
                    rootComponent.removeChild(rootComponent.childById(Component::class.java, "SelectionList"))
                    if (it) {
                        selectedStructure = data
                        onSelectedChanged.forEach { it(data) }
                    } else {
                        if (selectedStructure == data) {
                            selectedStructure = null
                            onSelectedChanged.forEach { it(null) }
                        }
                    }
                    rootComponent.child(1, selectionGrid())
                }
                grid.child(checkBox, index, 0)
                grid.child(
                    Components.label(
                        Text.literal(data.name)
                    ),
                    index, 1
                )
                if (litematicaInstalled) {
                    grid.child(
                        Components.button(
                            Text.translatable("reden.widget.rvc.structure.export.litematica")
                        ) {
                            data.collectFromWorld()
                            LitematicaIO.save(Path.of("schematics"), data)
                        },
                        index, 2
                    )
                }
            }
            return Containers.verticalScroll(
                Sizing.fill(100),
                Sizing.fill(50),
                grid
            ).id("SelectionList")
        }
        changeListener = {
            rootComponent.removeChild(rootComponent.children().last())
            rootComponent.child(
                Containers.verticalScroll(
                    Sizing.fill(100),
                    Sizing.fill(40),
                    Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply {
                        selectedStructure?.run {
                            child(Components.label(Text.translatable("reden.widget.rvc.structure.name", name)))
                            child(Components.label(Text.translatable("reden.widget.rvc.structure.block_count", blocks.count())))
                            child(Components.label(Text.translatable("reden.widget.rvc.structure.entity_count", entities.count())))
                            if (fluidScheduledTicks.isNotEmpty() || blockScheduledTicks.isNotEmpty() || blockEvents.isNotEmpty()) {
                                child(Components.label(Text.translatable("reden.widget.rvc.structure.scheduled_tick_unstable").red()))
                            }
                        }
                    }
                )
            )
        }
        onSelectedChanged.add(changeListener!!)
        rootComponent.child(selectionGrid())
        rootComponent.child(Components.label(Text.empty()))
    }
}
