package com.github.zly2006.reden.behalf

import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.SPNHAccess
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

fun registerBehalf(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(
        literal("behalf").then(
            argument("player", EntityArgumentType.player())
                .requires { it.hasPermissionLevel(4) && it.isExecutedByPlayer }
                .executes {
                    val networkHandler = it.source.player!!.networkHandler
                    val access = networkHandler as SPNHAccess
                    if (access.behalfPlayer != null) {
                        access.behalfPlayer?.data()?.behalfBy = null
                    }
                    val targetPlayer = EntityArgumentType.getPlayer(it, "player")
                    networkHandler.sendPacket(EntitiesDestroyS2CPacket(targetPlayer.id))
                    access.behalfPlayer = targetPlayer
                    targetPlayer.data().behalfBy = it.source.player!!
                    1
                }
        )
    )
}
