package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.ImguiScreen
import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.renderers
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.PlacementInfo
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.utils.red
import imgui.ImGui
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

val selectedStructure: TrackedStructure?
    get() {
        try {
            val structure = selectedRepository?.head()
            if (structure != null) {
                requireNotNull(structure.placementInfo) { "Structure ${structure.name} is not placed" }
                requireNotNull(structure.networkWorker) { "Network worker is not initialized for ${structure.name}" }
            }
            return structure
        } catch (e: Exception) {
            UIErrorToast.report("Error while getting selected structure")
            Reden.LOGGER.error("Error while getting selected structure", e)
            selectedRepository = null
            return null
        }
    }
var selectedRepository: RvcRepository? = null

class SelectionListScreen : ImguiScreen({
    ImGui.beginMainMenuBar()

    ImGui.beginMenu("RVC")
    ImGui.menuItem("New", "")
    ImGui.endMenu()

    ImGui.beginMenu("Git")
    ImGui.endMenu()

    ImGui.endMainMenuBar()

    ImGui.textWrapped("This is a test")
}) {
    init {
        renderers["selectionListScreen"] = {
            ImGui.textWrapped("aaaa")
        }
    }
}

class SelectionListScreen1 : BaseOwoScreen<FlowLayout>() {
    private var _selectedUIElement: RepositoryLine? = null
    var selectedUIElement: RepositoryLine?
        get() = _selectedUIElement
        set(value) {
            if (value != _selectedUIElement) {
                _selectedUIElement?.select?.checked(false)
                _selectedUIElement = value
                BlockOutline.blocks = mapOf()
                BlockBorder.tags = mapOf()
            }
            selectedRepository = value?.repository?.apply {
                if (placementInfo == null) {
                    // initialize placement info
                    placementInfo = PlacementInfo(client!!.getWorldInfo())
                }
                value.repository.head().run {
                    networkWorker?.launch {
                        refreshPositions()
                    }
                }
            }
            infoBox = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply {
                fun childTr(key: String, vararg args: Any) = child(Components.label(Text.translatable(key, *args)))
                selectedStructure?.run {
                    childTr("reden.widget.rvc.structure.name", name)
                    childTr("reden.widget.rvc.structure.block_count", totalBlocks)
                    childTr("reden.widget.rvc.structure.entity_count", entities.count())
                    if (regions.values.any { it.fluidScheduledTicks.isNotEmpty() }
                        || regions.values.any { it.blockScheduledTicks.isNotEmpty() }
                        || regions.values.any { it.blockEvents.isNotEmpty() }) {
                        childTr("reden.widget.rvc.structure.scheduled_tick_unstable")
                    }
                }
            }
        }
    private val worldInfo = MinecraftClient.getInstance().getWorldInfo()
    private val reloadAllButton = Components.button(Text.literal("Reload All")) {
        onFunctionUsed("reloadAll_rvcListScreen")
        selectedRepository = null
        client!!.data.rvc.load()
        close()
    }
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    inner class RepositoryLine(val repository: RvcRepository) :
        FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        override fun mount(parent: ParentComponent?, x: Int, y: Int) {
            super.mount(parent, x, y)
        }
        private val sameWorld = repository.placementInfo?.worldInfo?.equals(worldInfo)
        val select: CheckboxComponent = Components.checkbox(Text.empty()).apply {
            onChanged {
                selectedUIElement = if (it) this@RepositoryLine else null
            }
        }

        private val placeButton: ButtonComponent = Components.button(Text.literal("Place")) {
            onFunctionUsed("place_rvcStructure")
            repository.startPlacing(repository.head())
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
            MinecraftClient.getInstance().setScreen(SelectionExportScreen(this@SelectionListScreen1, repository))
        }
        private val removeButton = Components.button(Text.literal("Remove")) {
            onFunctionUsed("remove_rvcStructure")
            repository.head().clearArea()
            repository.clearCache()
            repository.placementInfo = null
            selectedUIElement = null
            close()
        }.apply {
            tooltip(Text.literal("Remove the structure from the current world, this does not delete the structure"))
        }
        val left = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        val right = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        private fun checkActive() {
            if (sameWorld == false) {
                this.tooltip(Text.literal("Not in the same world").red())
            }
            placeButton.active(sameWorld != true)
            removeButton.active(sameWorld == true)
            if (sameWorld != true) {
                select.tooltip(Text.literal("Please place this structure in the current world first"))
                select.checked(false)
                select.active = false
            }
            else {
                select.tooltip(Text.empty())
                select.active = true
            }
        }

        init {
            checkActive()
            gap(5)
            horizontalSizing(Sizing.fill())
            left.gap(5).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            right.gap(5).alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)

            left.child(select)
            left.child(Components.label(Text.literal(repository.name)))
            right.child(detailsButton)
            right.child(exportButton)
            right.child(placeButton)
            right.child(removeButton)

            child(left)
            child(right)
        }
    }

    /**
     * root -> scroll -> infBox
     */
    @Suppress("UNCHECKED_CAST")
    private var infoBox: FlowLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
        set(value) {
            val parent = field.parent() as ScrollContainer<Component>
            field = value
            parent.child(field)
        }

    override fun build(rootComponent: FlowLayout) {
        val mc = MinecraftClient.getInstance()
        val addRepository = Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(20)).apply {
            gap(5)
            child(Components.button(Text.literal("New")) {
                onFunctionUsed("new_rvcListScreen")
                client!!.setScreen(SelectionCreateScreen(this@SelectionListScreen1))
            })
            child(Components.button(Text.literal("Import")) {
                onFunctionUsed("import_rvcListScreen")
                client!!.setScreen(SelectionImportScreen())
            })
            child(reloadAllButton)
        }
        val repositoryLines = Containers.verticalFlow(Sizing.fill(), Sizing.content())
        val infoBoxScroll = Containers.verticalScroll(
            Sizing.fill(100),
            Sizing.fill(20),
            infoBox
        )

        mc.data.rvc.repositories.values.forEach {
            val element = RepositoryLine(it)
            if (it == selectedRepository) {
                _selectedUIElement = element
            }
            repositoryLines.child(element)
        }

        rootComponent
            .children(
                listOf(
                    addRepository,
                    Containers.verticalScroll(Sizing.fill(), Sizing.fill(70), repositoryLines),
                    infoBoxScroll
                )
            )
            .gap(5)
            .padding(Insets.of(5))
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
    }

    // work around. I am finding a better way to implement (including PR to owo)
    override fun init() {
        super.init()
        _selectedUIElement?.select?.checked(true)
    }
}
