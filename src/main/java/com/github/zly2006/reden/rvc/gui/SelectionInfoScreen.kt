package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.ImguiScreen
import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.renderers
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiTableFlags
import imgui.type.ImString
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import org.eclipse.jgit.revwalk.RevCommit
import java.text.SimpleDateFormat

/**
 * Edit trackpoints
 * Edit name
 * Edit description
 * Auto-backup (commit)
 */
class SelectionInfoScreen(
    val repository: RvcRepository,
    val structure: TrackedStructure,
    val parent: Screen? = null
) : ImguiScreen() {
    init {
        mainRenderer = {
            ImGui.text("Name: ${structure.name}")

            if (ImGui.button("Commit...")) {
                onFunctionUsed("commit_rvcStructure")
                val message = ImString()
                renderers["Commit"] = {
                    ImGui.text("Message:")
                    ImGui.inputTextMultiline("##message", message)

                    ImGui.textWrapped(
                        """
                        Committing will save the current state of the structure to the repository, then you can push the changes to the remote.
                        Committing as ${client!!.session.username} (${client!!.session.uuidOrNull})
                    """.trimIndent()
                    )

                    if (ImGui.button("Commit")) {
                        structure.networkWorker?.launch {
                            repository.commit(structure, message.get(), client!!.player)
                            renderers -= "Commit"
                        }
                    }
                    ImGui.sameLine()
                    if (ImGui.button("Cancel")) {
                        renderers -= "Commit"
                    }
                }
            }
            ImGui.sameLine()
            if (ImGui.button("Remotes...")) {
                onFunctionUsed("remotes_rvcStructure")
                val texts = repository.git.repository.config.getSubsections("remote").associateWith {
                    ImString(repository.git.repository.config.getString("remote", it, "url")).apply {
                        inputData.isResizable = true
                    }
                }.toMutableMap()
                renderers["Remotes"] = {
                    if (ImGui.button("Add")) {
                        val name = ImString()
                        renderers["Add Remote"] = {
                            ImGui.text("Name")
                            ImGui.sameLine()
                            ImGui.inputText("##name", name)
                            val valid = name.get() !in texts
                            if (!valid) {
                                ImGui.textColored(1f, 0f, 0f, 1f, "Name already exists")
                            }
                            if (!valid) ImGui.beginDisabled()
                            if (ImGui.button("OK")) {
                                texts[name.get()] = ImString()
                                renderers -= "Add Remote"
                            }
                            if (!valid) ImGui.endDisabled()
                            ImGui.sameLine()
                            if (ImGui.button("Cancel")) {
                                renderers -= "Add Remote"
                            }
                        }
                    }
                    if (ImGui.beginTable("Remotes", 2)) {
                        ImGui.tableSetupColumn("Name", 0)
                        ImGui.tableSetupColumn("URL", 0)
                        ImGui.tableHeadersRow()
                        texts.forEach { (name, url) ->
                            ImGui.tableNextRow()
                            ImGui.tableSetColumnIndex(0)
                            ImGui.text(name)
                            ImGui.tableSetColumnIndex(1)
                            ImGui.inputText("##url_$name", url)
                        }
                        ImGui.endTable()
                    }
                    if (ImGui.button("OK")) {
                        val toRemove = repository.git.repository.config.getSubsections("remote") - texts.keys
                        texts.forEach { (name, url) ->
                            repository.git.repository.config.setString("remote", name, "url", url.get())
                        }
                        toRemove.forEach { name ->
                            repository.git.repository.config.removeSection("remote", name)
                        }
                        renderers -= "Remotes"
                    }
                    if (ImGui.button("Cancel")) {
                        renderers -= "Remotes"
                    }
                }
            }
            ImGui.sameLine()
            if (ImGui.button("Fetch")) {
                onFunctionUsed("fetch_rvcStructure")
                repository.fetch()
            }
            ImGui.sameLine()
            if (ImGui.button("Push")) {
                onFunctionUsed("push_rvcStructure")
                try {
                    repository.push(repository.remote, ChatScreen.hasShiftDown())
                } catch (e: Exception) {
                    Reden.LOGGER.error("Failed to push ${repository.name}", e)
                    UIErrorToast.report(e)
                }
            }
            ImGui.sameLine()
            if (ImGui.button("Pull")) {
                onFunctionUsed("pull_rvcStructure")
                TODO()
            }
            ImGui.sameLine()

            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0xFF0000FF.toInt())
            if (ImGui.button("Delete")) {
                onFunctionUsed("delete_rvcStructure")
                renderers["Delete Repository"] = {
                    ImGui.text("Are you sure you want to delete ${repository.name}?")
                    if (ImGui.button("Yes")) {
                        client!!.data.rvc.repositories.remove(repository.name)
                        repository.delete()
                        client!!.setScreen(SelectionListScreen())
                        renderers -= "Delete Repository"
                    }
                    if (ImGui.button("No")) {
                        renderers -= "Delete Repository"
                    }
                }
            }
            ImGui.popStyleColor()

            if (repository.git.branchList().call().isEmpty()) {
                ImguiRedText("No commits")
            } else {
                ImGui.text("Head: ${repository.headHash.substring(0, 7)} on ${repository.headBranch}")
                if (ImGui.beginTable("Commits", 5)) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm")
                    ImGui.tableSetupColumn("Hash", 0)
                    ImGui.tableSetupColumn("Message", 0)
                    ImGui.tableSetupColumn("Time", 0)
                    ImGui.tableSetupColumn("Author", 0)
                    ImGui.tableSetupColumn("Operations", 0)

                    ImGui.tableHeadersRow()

                    val allCommits = repository.git.log().call().filterIsInstance<RevCommit>()
                    allCommits.forEach { commit ->
                        ImGui.pushID(commit.name)
                        ImGui.tableNextRow()
                        ImGui.tableSetColumnIndex(0)
                        ImGui.text(commit.name.substring(0, 7))
                        ImGui.tableSetColumnIndex(1)
                        ImGui.text(commit.shortMessage)
                        ImGui.tableSetColumnIndex(2)
                        ImGui.text(dateFormat.format(commit.commitTime * 1000L))
                        ImGui.tableSetColumnIndex(3)
                        ImGui.text(commit.authorIdent.name)
                        ImGui.tableSetColumnIndex(4)
                        if (ImGui.button("Checkout")) {
                            onFunctionUsed("checkout_rvcStructure")
                            structure.clearArea()
                            repository.checkout(commit.name, repository::configure)
                            structure.paste()
                            client!!.setScreen(SelectionListScreen())
                        }
                        ImGui.popID()
                    }
                    ImGui.endTable()
                }
            }

            if (ImGui.beginTable("Regions", 3)) {
                ImGui.tableSetupColumn("Name", 0)
                ImGui.tableSetupColumn("Trackers", 0)
                ImGui.tableSetupColumn("Operations", 0)
                ImGui.tableHeadersRow()
                structure.regions.values.forEach { region ->
                    ImGui.pushID(region.name)
                    ImGui.tableNextRow()
                    ImGui.tableSetColumnIndex(0)
                    ImGui.text(region.name)
                    ImGui.tableSetColumnIndex(1)
                    ImGui.text("${region.tracker::class.java.simpleName} (${region.xSize}x${region.ySize}x${region.zSize})")
                    ImGui.tableSetColumnIndex(2)
                    if (ImGui.button("Edit")) {
                        onFunctionUsed("edit_rvcStructure")
                        val popupName = "Edit \"${region.name}\""
                        renderers[popupName] = {
                            ImGui.text("Name: ${region.partName}, subregion of ${region.structure.name}")
                            ImGui.text("Git submodule: ${null} // todo")
                            ImGui.text("Tracker: ${region.tracker::class.java.simpleName}")
                            if (region.tracker is StructureTracker.Trackpoint) {
                                if (ImGui.beginTable("Trackpoints", 4, ImGuiTableFlags.ScrollX)) {
                                    ImGui.tableSetupColumn("Position")
                                    ImGui.tableSetupColumn("Predicate")
                                    ImGui.tableSetupColumn("Mode")
                                    ImGui.tableSetupColumn("Operations")
                                    ImGui.tableHeadersRow()

                                    region.tracker.trackpoints.forEach { trackpoint ->
                                        ImGui.pushID(trackpoint.pos.asLong())
                                        ImGui.tableNextRow()
                                        ImGui.tableSetColumnIndex(0)
                                        ImGui.text(trackpoint.pos.toShortString())
                                        ImGui.tableSetColumnIndex(1)
                                        ImGui.text(trackpoint.predicate.name)
                                        ImGui.tableSetColumnIndex(2)
                                        ImGui.text(trackpoint.mode.name)
                                        ImGui.tableSetColumnIndex(3)
                                        if (ImGui.button("Delete")) {
                                            region.tracker.removeTrackpoint(trackpoint.pos)
                                            renderers["Restore deleted trackpoint"] = {
                                                ImGui.text("Trackpoint deleted")
                                                ImGui.text("If you want to restore it, click the button below")
                                                if (ImGui.button("Restore")) {
                                                    region.tracker.addTrackPoint(trackpoint)
                                                    renderers -= "Restore deleted trackpoint"
                                                }
                                            }
                                        }
                                        ImGui.popID()
                                    }
                                    ImGui.endTable()
                                }
                            }
                            if (ImGui.button("Close")) {
                                renderers -= popupName
                            }
                        }
                    }
                    ImGui.popID()
                }
                ImGui.endTable()
            }
        }
    }

    override fun close() {
        client!!.setScreen(parent)
    }
}
