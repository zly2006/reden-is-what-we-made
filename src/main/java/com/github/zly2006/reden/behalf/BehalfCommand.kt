package com.github.zly2006.reden.behalf

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(
        literal("behalf").then(
            argument("player", EntityArgumentType.player())
                .requires { it.hasPermissionLevel(4) && it.isExecutedByPlayer }
                .executes {
                    val networkHandler = it.source.player!!.networkHandler
                    1
                }
        )
    )
}
