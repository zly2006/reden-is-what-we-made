package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.gui.git.RvcManageRemotesScreen
import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text
import org.eclipse.jgit.revwalk.RevCommit

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
    private val deleteButton = Components.button(Text.literal("Delete")) {
        // todo confirm screen
        onFunctionUsed("delete_rvcStructure")
        if (selectedRepository == repository) {
            selectedRepository = null
        }
        client!!.data.rvcStructures.remove(repository.name)
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
        val remote = object : IRemoteRepository {
            override fun deleteRepo() {
                TODO("Not yet implemented")
            }

            override val gitUrl = repository.git.repository.config.getString("remote", "origin", "url")
        }
        repository.push(remote)
    }!!
    private val pullButton = Components.button(Text.literal("Pull")) {
        onFunctionUsed("pull_rvcStructure")
        TODO()
    }!!

    inner class ReversionLine(
        val commit: RevCommit
    ) : FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        private val shortHash = Components.label(Text.literal(commit.name.substring(0, 7)))
        private val message = Components.label(Text.literal(commit.shortMessage))!!.apply {
            sizing(Sizing.fill(70), Sizing.fixed(20))
            verticalTextAlignment(VerticalAlignment.CENTER)
        }
        private val time = Components.label(Text.literal(commit.commitTime.toString()))!!
        private val author = Components.label(Text.literal(commit.authorIdent.name))!!
        private val checkoutButton = Components.button(Text.literal("Checkout")) {
            onFunctionUsed("checkout_rvcStructure")
            structure.clearArea()
            repository.checkout(commit.name)
            structure.paste()
            client!!.setScreen(SelectionListScreen())
            // todo
        }!!

        init {
            verticalAlignment(VerticalAlignment.CENTER)
            gap(5)
            child(shortHash)
            child(message)
            child(time)
            child(author)
            child(checkoutButton)
        }
    }

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        val commits = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
            val allCommits = repository.git.log().call().filterIsInstance<RevCommit>()
            allCommits.forEach {
                child(ReversionLine(it))
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
                child(deleteButton)
                child(remotesButton)
                child(fetchButton)
                child(pushButton)
                child(pullButton)
            })
            .child(
                Components.label(
                    if (repository.git.branchList().call().isNotEmpty())
                        Text.literal(
                            "Head: ${
                                repository.headHash.substring(
                                    0,
                                    7
                                )
                            } on ${repository.headBranch}"
                        )
                    else Text.literal("No commits").red()
                )
            )
            .child(Containers.verticalScroll(Sizing.fill(), Sizing.fill(80), commits))
    }
}
