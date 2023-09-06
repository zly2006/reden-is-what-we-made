package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.*
import kotlinx.atomicfu.atomic
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


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
    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this) { horizontalSizing, verticalSizing ->
            Containers.verticalFlow(horizontalSizing, verticalSizing)
        }
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
        val listComponent = atomic<FlowLayout?>(null)
        fun update(remove: Boolean = true) {
            if (remove) rootComponent.removeChild(rootComponent.children().last())
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
        listComponent.value = Components.list(
            trackedStructureList,
            { },
            { data ->
                val c = Containers.grid(Sizing.content(), Sizing.content(), 1, 3)
                val checkBox = Components.checkbox(Text.empty())
                checkBox.onChanged {
                    if (it) {
                        listComponent.value?.children()?.forEach { component ->
                            if (c != component) {
                                component as GridLayout
                                (component.children().first() as CheckboxComponent).checked(false)
                                selectedStructure = data
                                update()
                            }
                        }
                    }
                    else {
                        // cancel
                        selectedStructure = null
                        update()
                    }
                }
                c.child(checkBox, 0, 0)
                c.child(
                    Components.button(
                        Text.literal(data.name)
                    ) {
                      // todo
                    },
                    0, 1
                )
            },
            true
        )
        rootComponent.child(
            Containers.verticalScroll(
                Sizing.fill(100),
                Sizing.fill(50),
                listComponent.value
            )
        )
        update(false)
    }
}

private fun MutableText.red() = formatted(Formatting.RED)
