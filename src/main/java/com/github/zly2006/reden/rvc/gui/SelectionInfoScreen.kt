package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.exceptions.RedenException
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.gui.git.RvcCommitScreen
import com.github.zly2006.reden.rvc.gui.git.RvcManageRemotesScreen
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UIErrorToast
import net.minecraft.client.gui.screen.ChatScreen
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
    val structure: TrackedStructure
): BaseOwoScreen<FlowLayout>() {
    private val commitButton = Components.button(Text.literal("Commit")) {
        onFunctionUsed("commit_rvcStructure")
        client!!.setScreen(RvcCommitScreen(repository, structure))
    }!!
    private val deleteButton = Components.button(Text.literal("Delete").red()) {
        // todo confirm screen
        onFunctionUsed("delete_rvcStructure")
        if (selectedRepository == repository) {
            selectedRepository = null
        }
        client!!.data.rvc.repositories.remove(repository.name)
        repository.delete()
        client!!.setScreen(SelectionListScreen())
    }!!
    private val remotesButton = Components.button(Text.literal("Remotes")) {
        onFunctionUsed("remotes_rvcStructure")
        client!!.setScreen(RvcManageRemotesScreen(repository, this))
    }!!
    private val fetchButton = Components.button(Text.literal("Fetch")) {
        onFunctionUsed("fetch_rvcStructure")
        repository.fetch()
    }!!
    private val pushButton = Components.button(Text.literal("Push")) {
        onFunctionUsed("push_rvcStructure")
        try {
            repository.push(repository.remote, ChatScreen.hasShiftDown())
        } catch (e: Exception) {
            Reden.LOGGER.error("Failed to push ${repository.name}", e)
            UIErrorToast.report(e)
        }
    }!!.apply {
        tooltip(Text.literal("Hold shift to force push"))
    }
    private val pullButton = Components.button(Text.literal("Pull")) {
        onFunctionUsed("pull_rvcStructure")
        TODO()
    }!!
    private val createLicenseMenu = Components.dropdown(Sizing.content()).apply {
        listOf(
            "All rights reserved",
            "CC 0",
            "CC 4.0 BY",
            "CC 4.0 BY SA",
            "CC 4.0 BY NC",
            "CC 4.0 BY NC SA",
            "CC 4.0 BY NC ND",
        ).map { it to it.replace(" ", "-").lowercase() }.forEach { pair ->
            button(Text.literal(pair.first)) {
                try {
                    repository.createLicense(
                        "assets/rvc/licenses/${pair.second}.txt",
                        client!!.player!!.nameForScoreboard
                    )
                } catch (e: RedenException) {
                    UIErrorToast.report(e)
                }
            }
        }
    }
    private val createLicenseButton = Components.button(Text.literal("Create License")) {

    }

    override fun close() {
        client!!.setScreen(SelectionListScreen())
    }

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
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
        rootComponent
            .gap(5)
            .padding(Insets.of(10))
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)

        rootComponent.child(Components.label(Text.literal("Name: ${structure.name}")))
            .child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                gap(5)
                child(commitButton)
                child(remotesButton)
                child(fetchButton)
                child(pushButton)
                child(pullButton)
                child(deleteButton)
            })
            .child(
                Components.label(
                    if (repository.git.branchList().call().isNotEmpty())
                        Text.literal(
                            "Head: ${repository.headHash.substring(0, 7)} on ${repository.headBranch}"
                        )
                    else Text.literal("No commits").red()
                )
            )
            .child(Containers.verticalScroll(Sizing.fill(), Sizing.fill(80), commits))
    }

    fun refresh() {

    }
}
