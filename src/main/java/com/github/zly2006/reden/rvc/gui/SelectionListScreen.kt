package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.redenError
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
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
    private val worldInfo = MinecraftClient.getInstance().getWorldInfo()
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    inner class RepositoryLine(private val repository: RvcRepository) :
        FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        private val sameWorld = repository.placementInfo?.worldInfo?.equals(worldInfo)
        var canPlace = repository.placementInfo == null || !repository.placed
            set(value) {
                field = value
                checkActive()
            }
        val select: CheckboxComponent = Components.checkbox(Text.empty()).apply {
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
            checked(selectedRepository == repository)
        }
        private val saveButton: ButtonComponent = Components.button(Text.literal("Save")) {
            onFunctionUsed("commit_rvcStructure")
            repository.head().collectFromWorld()
            if (repository.head().blocks.isEmpty()) {
                redenError(Text.translatable("reden.rvc.message.save.empty_structure").red())
            }
            // todo: commit message
            repository.commit(repository.head(), "RedenMC RVC Commit", MinecraftClient.getInstance().player)
            client?.player?.sendMessage(Text.translatable("reden.rvc.message.save.ok"))
            it.active(false)
        }
        private val placeButton: ButtonComponent = Components.button(Text.literal("Place")) {
            onFunctionUsed("place_rvcStructure")
            repository.startPlacing()
            it.active(false)
        }.apply {
            if (sameWorld == false) {
                tooltip(
                    Text.literal(
                        """
                Note: We have detected that this machine has been placed in a different world.
                It is recommended to remove it first and then place it again.
                """.trimIndent()
                    )
                )
            }
        }
        private val detailsButton: ButtonComponent = Components.button(Text.literal("Details")) {
            onFunctionUsed("open_rvcStructure")
            MinecraftClient.getInstance().setScreen(SelectionInfoScreen(repository, repository.head()))
        }
        private val exportButton: ButtonComponent = Components.button(Text.literal("Export")) {
            onFunctionUsed("export_rvcStructure")
            MinecraftClient.getInstance().setScreen(SelectionExportScreen(this@SelectionListScreen, repository))
        }
        val left = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        val right = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private fun checkActive() {
            saveButton.active(repository.hasChanged() && sameWorld == true)
            placeButton.active(canPlace)
            select.active = sameWorld == true
            if (sameWorld == false) {
                select.checked(false)
                this.tooltip(Text.literal("Not in the same world").red())
            }
        }

        init {
            checkActive()
            gap(5)
            left.gap(5).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            right.gap(5).alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
            child(left)
            child(right)

            left.child(select)
            left.child(Components.label(Text.literal(repository.name)))
            right.child(Components.button(Text.literal("Delete")) {
                onFunctionUsed("delete_rvcStructure")
                if (selectedRepository == repository) {
                    selectedRepository = null
                    selectedUIElement = null
                }
                client!!.data.rvcStructures.remove(repository.name)
                repository.delete()
                parent!!.removeChild(this)
            })
            right.child(detailsButton)
            right.child(exportButton)
            right.child(saveButton)
            right.child(placeButton)
        }
    }

    private var infoBox: Component? = null

    override fun build(rootComponent: FlowLayout) {
        val mc = MinecraftClient.getInstance()
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
            gap(5)
            child(Components.button(Text.literal("New")) {
                onFunctionUsed("new_rvcListScreen")
                client!!.setScreen(SelectionCreateScreen())
            })
            child(Components.button(Text.literal("Import")) {
                onFunctionUsed("import_rvcListScreen")
                client!!.setScreen(SelectionImportScreen())
            })
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
