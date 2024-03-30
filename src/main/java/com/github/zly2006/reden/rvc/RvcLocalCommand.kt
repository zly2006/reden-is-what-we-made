package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.rvc.gui.selectedRepository
import com.github.zly2006.reden.rvc.gui.selectedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.redenmc.bragadier.ktdsl.register
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.eclipse.jgit.api.Git
import java.net.URI

private fun CommandContext<*>.string(name: String) =
    try {
        getString(this, name)
    } catch (e: IllegalArgumentException) {
        null
    }

fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val mc = MinecraftClient.getInstance()
    dispatcher.register {
        literal("rvc-local") {
            literal("use") {
                argument("name", string()).suggest { mc.data.rvcStructures.keys }.executes {
                    val name = it.string("name")
                    selectedRepository = mc.data.rvcStructures[name] ?: error("No such repository: $name")
                    if (selectedRepository!!.placementInfo?.worldInfo != mc.getWorldInfo()) {
                        selectedRepository = null
                        error("Repository $name is not in the current world")
                    }
                    1
                }
            }
            literal("region") {
                literal("import") {
                    optional(argument("name", string())) {
                        argument("url", greedyString()).executes {
                            selectedRepository ?: error("No repository selected")
                            val url = it.string("url")!!
                            val name = it.string("name") ?: URI(url).path.split("/").last().let { name ->
                                require(selectedRepository!!.head().regions[name] == null) {
                                    error("Submodule $name already exists")
                                }
                                name
                            }
                            it.source.sendMessage("Importing $name from $url")
                            selectedRepository!!.git.repository.workTree.resolve(name).deleteRecursively()
                            selectedRepository!!.git.submoduleAdd()
                                .setName(name)
                                .setPath(name)
                                .setURI(url)
                                .call()
                            selectedRepository!!.clearCache()
                            requireNotNull(selectedRepository!!.head().regions[name]) {
                                error("Submodule $name was not added successfully")
                            }
                            let { selectedStructure!!.regions[""]!!.blockIterator }.asSequence()
                            1
                        }
                    }
                }
                literal("list").executes {
                    selectedRepository ?: error("No repository selected")
                    it.source.sendMessage(selectedRepository!!.head().regions.keys.joinToString(prefix = "Regions: "))
                    1
                }
                literal("remove") {
                    argument("name", string()).suggest { selectedRepository!!.head().regions.keys }.executes {
                        selectedRepository ?: error("No repository selected")
                        val name = it.string("name")!!
                        Git.open(selectedRepository!!.git.repository.workTree.resolve(name))
                            .submoduleDeinit()
                            .call()
                        selectedRepository!!.git.repository.workTree.resolve(name).deleteRecursively()
                        selectedRepository!!.clearCache()
                        require(selectedRepository!!.head().regions[name] == null) {
                            error("Submodule $name was not removed successfully")
                        }
                        1
                    }
                }
                literal("new")
                literal("update")
            }
            literal("commit") {
                argument("message", greedyString()).executes {
                    selectedRepository ?: error("No repository selected")
                    val message = it.string("message")
                    selectedRepository!!.git.commit().setMessage(message).call()
                    1
                }
            }
            literal("push").executes {
                selectedRepository ?: error("No repository selected")
                selectedRepository!!.git.push().call()
                1
            }
            literal("pull").executes {
                selectedRepository ?: error("No repository selected")
                selectedRepository!!.git.pull().call()
                1
            }
            literal("git-status").executes {
                selectedRepository ?: error("No repository selected")
                selectedRepository!!.git.status().call().let { status ->
                    it.source.sendMessage("Added (${status.added.size}):")
                    status.added.forEach(it.source::sendMessage)
                    it.source.sendMessage("Changed (${status.changed.size}):")
                    status.changed.forEach(it.source::sendMessage)
                    it.source.sendMessage("Removed (${status.conflicting.size}):")
                    status.removed.forEach(it.source::sendMessage)
                    it.source.sendMessage("Untracked (${status.removed.size}):")
                    status.untracked.forEach(it.source::sendMessage)
                }
                1
            }
            literal("log").executes { context ->
                selectedRepository ?: error("No repository selected")
                selectedRepository!!.git.log().call().forEach {
                    context.source.sendMessage(
                        "${
                            it.name.substring(
                                0,
                                7
                            )
                        }: ${it.shortMessage} at ${it.commitTime} by ${it.authorIdent.name}"
                    )
                }
                1
            }
            literal("checkout")
            literal("branch") {
                literal("ls").executes { context ->
                    selectedRepository ?: error("No repository selected")
                    selectedRepository!!.git.branchList().call().forEach {
                        it.name.let { name ->
                            if (it.name == selectedRepository!!.git.repository.branch) {
                                selectedRepository!!.git.log().add(it.objectId).call().first().let { commit ->
                                    context.source.sendMessage("* $name: ${commit.shortMessage}")
                                }
                            }
                            else {
                                context.source.sendMessage(name)
                            }
                        }
                    }
                    1
                }
            }
        }
    }
}

private fun RequiredArgumentBuilder<*, *>.suggest(keys: () -> Collection<String>) = suggests { _, builder ->
    keys().forEach { builder.suggest(it) }
    builder.buildFuture()
}

private fun FabricClientCommandSource.sendMessage(s: String) {
    sendFeedback(Text.literal(s))
}
