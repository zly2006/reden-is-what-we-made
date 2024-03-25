package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.rvc.gui.selectedRepository
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.eclipse.jgit.api.Git
import java.net.URI

private fun CommandContext<*>.string(name: String) = getString(this, name)

@Suppress("NOTHING_TO_INLINE")
inline fun error(reason: String): Nothing =
    throw SimpleCommandExceptionType(Text.literal(reason)).create()

fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val mc = MinecraftClient.getInstance()
    dispatcher.register(ClientCommandManager.literal("rvc-local").then {
        literal("use").then {
            argument("name", string()).executes {
                val name = it.string("name")
                selectedRepository = mc.data.rvcStructures[name] ?: error("No such repository: $name")
                1
            }
        }
        literal("region").then {
            literal("import").then {
                argument("url", greedyString()).executes {
                    selectedRepository ?: error("No repository selected")
                    val url = it.string("url")
                    val name = URI(url).path.split("/").last().let { name ->
                        require(selectedRepository!!.head().regions[name] == null) {
                            error("Submodule $name already exists")
                        }
                        name
                    }
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
                    1
                }.then {
                    argument("name", string()).executes {
                        selectedRepository ?: error("No repository selected")
                        val url = it.string("url")
                        val name = it.string("name")
                        1
                    }
                }
            }
            literal("list").executes {
                selectedRepository ?: error("No repository selected")
                selectedRepository!!.head().regions.keys.forEach(::println)
                1
            }
            literal("remove").then {
                argument("name", string()).executes {
                    selectedRepository ?: error("No repository selected")
                    val name = it.string("name")
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
        literal("commit").then {
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
        literal("status").executes {
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
        literal("log")
        literal("checkout")
        literal("branch").then {
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
    } as LiteralArgumentBuilder<FabricClientCommandSource>)
}

private fun FabricClientCommandSource.sendMessage(s: String) {
    sendFeedback(Text.literal(s))
}

@DslMarker
annotation class CommandBuilder

@CommandBuilder
class BuilderScope<S>(
    val node: ArgumentBuilder<S, *>,
    val builders: MutableList<ArgumentBuilder<S, *>> = mutableListOf()
) {
    @CommandBuilder
    fun literal(name: String) = ClientCommandManager.literal(name)!!

    @CommandBuilder
    fun argument(name: String, type: ArgumentType<*>) = ClientCommandManager.argument(name, type)!!
}

private fun <S> ArgumentBuilder<S, *>.then(function: BuilderScope<S>.() -> Unit) = apply {
    val scope = BuilderScope(this)
    function(scope)
    scope.builders.forEach(::then)
}
