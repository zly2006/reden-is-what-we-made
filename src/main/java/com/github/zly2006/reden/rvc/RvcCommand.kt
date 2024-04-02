package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

fun registerRvc(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal("rvc")
                .executes {
                        1
                }
                .then(literal("commit")
                        .executes {
                                it.source.playerData()
                                1
                        }
                )
                .then(literal("use")
                        .then(argument("name", StringArgumentType.word())
                                .executes {
                                        val name = StringArgumentType.getString(it, "name")
                                        1
                                }
                        )
                )
        )
}

private fun ServerCommandSource.playerData() = player!!.data()
