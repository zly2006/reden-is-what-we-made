package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.ImguiScreen
import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.renderers
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import imgui.ImGui
import imgui.flag.ImGuiTableBgTarget
import imgui.flag.ImGuiTableColumnFlags
import imgui.flag.ImGuiTableFlags
import imgui.type.ImString
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.client.MinecraftClient
import net.minecraft.network.NetworkSide

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

fun ImguiRedText(text: String) {
    ImGui.textColored(1f, 0f, 0f, 1f, text)
}

class SelectionListScreen : ImguiScreen() {
    fun newRepository() {
        val name = ImString()
        renderers["New Repository"] = {
            ImGui.text("Name")
            ImGui.sameLine()
            ImGui.inputText("##name", name)

            val buttonCreate = ImGui.button("Create")
            if (name.get() in client!!.data.rvc.repositories) {
                ImGui.newLine()
                ImguiRedText("Name already exists")
            } else if (name.get().isEmpty()) {
                ImGui.newLine()
                ImguiRedText("Name cannot be empty")
            } else if (buttonCreate) {
                onFunctionUsed("create_rvcListScreen", true)
                val repository = RvcRepository.create(name.get(), client!!.getWorldInfo(), NetworkSide.CLIENTBOUND)
                client!!.data.rvc.repositories[name.get()] = repository
                selectedRepository = repository
                renderers -= "New Repository"
            }
        }
    }

    init {
        var renderHoveRedRow = 1
        val worldInfo = MinecraftClient.getInstance().getWorldInfo()
        mainRenderer = {
            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("RVC")) {
                    if (ImGui.menuItem("New", "")) {
                        newRepository()
                    }
                    if (ImGui.menuItem("Import", "I")) {
                        onFunctionUsed("import_rvcListScreen")
                        client!!.setScreen(SelectionImportScreen())
                    }
                    ImGui.endMenu()
                }
                if (ImGui.beginMenu("Git")) {
                    ImGui.endMenu()
                }
                ImGui.endMainMenuBar()
            }

            if (ImGui.button("Reload All Repositories")) {
                onFunctionUsed("reloadAll_rvcListScreen")
                selectedRepository = null
                client!!.data.rvc.load()
                close()
            }


            if (ImGui.beginTable("Repositories", 5, ImGuiTableFlags.ScrollY)) {
                ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch)
                ImGui.tableSetupColumn("", ImGuiTableColumnFlags.WidthFixed)
                ImGui.tableSetupColumn("", ImGuiTableColumnFlags.WidthFixed)
                ImGui.tableSetupColumn("", ImGuiTableColumnFlags.WidthFixed)
                ImGui.tableSetupColumn("", ImGuiTableColumnFlags.WidthFixed)
                ImGui.tableHeadersRow()
                ImGui.tableNextRow()
                ImGui.text("Name")

                client!!.data.rvc.repositories.values.forEachIndexed { index, repository ->
                    fun hoverHighlight() {
                        if (ImGui.isItemHovered() || ImGui.isItemActive()) {
                            renderHoveRedRow = index
                        }
                    }
                    ImGui.tableNextRow()
                    if (renderHoveRedRow == index) {
                        ImGui.tableSetBgColor(ImGuiTableBgTarget.RowBg1, 0x7fffffff)
                    }

                    // row start
                    ImGui.tableNextColumn()
                    if (ImGui.checkbox(repository.name, selectedRepository == repository)) {
                        selectedRepository = repository
                    }
                    hoverHighlight()

                    val sameWorld = repository.placementInfo?.worldInfo?.equals(worldInfo)
                    run { // Place button
                        if (sameWorld == true) {
                            ImGui.beginDisabled()
                        }
                        if (ImGui.button("Place")) {
                            onFunctionUsed("place_rvcStructure")
                            repository.startPlacing(repository.head())
                            close()
                        }
                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip()
                            ImGui.textWrapped(
                                """
                        Note: We have detected that this machine has been placed in a different world.
                        It is recommended to remove it first and then place it again.
                        """.trimIndent()
                            )
                            ImGui.endTooltip()
                        }
                        if (sameWorld == true) {
                            ImGui.endDisabled()
                        }
                    }
                    ImGui.tableNextColumn()
                    if (ImGui.button("Details")) {
                        onFunctionUsed("open_rvcStructure")
                        MinecraftClient.getInstance().setScreen(SelectionInfoScreen(repository, repository.head()))
                    }
                    ImGui.tableNextColumn()
                    if (ImGui.button("Export")) {
                        onFunctionUsed("export_rvcStructure")
                        MinecraftClient.getInstance().setScreen(SelectionExportScreen(this, repository))
                    }
                    ImGui.tableNextColumn()
                    run {
                        if (sameWorld != true) {
                            ImGui.beginDisabled()
                        }
                        if (ImGui.button("Remove")) {
                            onFunctionUsed("remove_rvcStructure")
                            repository.head().clearArea()
                            repository.clearCache()
                            repository.placementInfo = null
                        }
                        if (sameWorld != true) {
                            ImGui.endDisabled()
                        }
                    }
                }
                ImGui.endTable()
            }
            ImGui.textWrapped("This is a test")
        }
        renderers["selectionListScreen"] = {
            ImGui.textWrapped("aaaa")
        }
    }
}
