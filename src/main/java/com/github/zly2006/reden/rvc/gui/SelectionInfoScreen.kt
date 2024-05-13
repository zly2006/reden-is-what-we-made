package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.ImguiScreen
import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.gui.git.RvcCommitScreen
import com.github.zly2006.reden.rvc.gui.git.RvcManageRemotesScreen
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.red
import imgui.ImGui
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
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
    val parent: Screen
) : ImguiScreen() {
    init {
        mainRenderer = {
            ImGui.text("Name: ${structure.name}")

            if (ImGui.button("Commit...")) {
                onFunctionUsed("commit_rvcStructure")
                client!!.setScreen(RvcCommitScreen(repository, structure))
            }
            if (ImGui.button("Remotes...")) {
                onFunctionUsed("remotes_rvcStructure")
                client!!.setScreen(RvcManageRemotesScreen(repository, this))
            }
            if (ImGui.button("Fetch")) {
                onFunctionUsed("fetch_rvcStructure")
                repository.fetch()
            }
            if (ImGui.button("Push")) {
                onFunctionUsed("push_rvcStructure")
                try {
                    repository.push(repository.remote, ChatScreen.hasShiftDown())
                } catch (e: Exception) {
                    Reden.LOGGER.error("Failed to push ${repository.name}", e)
                    UIErrorToast.report(e)
                }
            }
            if (ImGui.button("Pull")) {
                onFunctionUsed("pull_rvcStructure")
                TODO()
            }
            if (ImGui.button("Delete")) {
                onFunctionUsed("delete_rvcStructure")
                // todo confirm screen
                if (selectedRepository == repository) {
                    selectedRepository = null
                }
                client!!.data.rvc.repositories.remove(repository.name)
                repository.delete()
                client!!.setScreen(SelectionListScreen())
            }

            if (repository.git.branchList().call().isNotEmpty()) {
                ImGui.text("Head: ${repository.headHash.substring(0, 7)} on ${repository.headBranch}")
            }
        }
    }

    override fun close() {
        client!!.setScreen(parent)
    }

    fun build(rootComponent: FlowLayout) {
        val commits = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
            if (repository.git.branchList().call().isEmpty()) {
                child(Components.label(Text.literal("No commits").red()))
            }
            else {
                gap(2)
                fun label(text: Text, size: Int) = Components.label(text).apply {
                    verticalTextAlignment(VerticalAlignment.CENTER)
                    horizontalSizing(Sizing.fill(size))
                }

                val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm")
                val allCommits = repository.git.log().call().filterIsInstance<RevCommit>()
                child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                    gap(2)
                    child(label(Text.literal("Hash"), 15))
                    child(label(Text.literal("Message"), 40))
                    child(label(Text.literal("Time"), 15))
                    child(label(Text.literal("Author"), 15))
                    child(label(Text.literal("Operation"), 15))
                })
                allCommits.forEach { commit ->
                    child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                        gap(2)
                        child(label(Text.literal(commit.name.substring(0, 7)), 15))
                        child(label(Text.literal(commit.shortMessage), 40))
                        child(label(Text.literal(dateFormat.format(commit.commitTime * 1000L)), 15))
                        child(label(Text.literal(commit.authorIdent.name), 15))
                        child(Components.button(Text.literal("Checkout")) {
                            onFunctionUsed("checkout_rvcStructure")
                            structure.clearArea()
                            repository.checkout(commit.name, repository::configure)
                            structure.paste()
                            client!!.setScreen(SelectionListScreen())
                        })
                    })
                }
            }
        }
    }
}
