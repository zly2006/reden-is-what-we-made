package com.github.zly2006.reden.rvc

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    dispatcher.register(ClientCommandManager.literal("rvc-local"))
}
