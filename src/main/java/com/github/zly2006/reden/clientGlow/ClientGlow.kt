package com.github.zly2006.reden.clientGlow

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.function.LazyIterationConsumer
import net.minecraft.util.math.Vec3d
import java.util.function.Consumer
import kotlin.math.min

@JvmField
var selector: EntitySelector? = null

fun selected(pos: Vec3d, sourceEntity: Entity?): List<Entity> {
    val selector = selector ?: return emptyList()
    val mc = MinecraftClient.getInstance()
    if (!selector.includesNonPlayers()) {
        return mc.world!!.players
    }
    else if (selector.playerName != null) {
        return mc.world!!.players.filter { it.entityName == selector.playerName }
    }
    else if (selector.uuid != null) {
        return mc.world!!.entities.filter { it.uuid == selector.uuid }
    }
    else {
        val predicate = selector.getPositionPredicate(selector.positionOffset.apply(pos))
        return if (selector.senderOnly) {
            if (sourceEntity != null && predicate.test(sourceEntity))
                listOf(sourceEntity)
            else
                emptyList()
        } else {
            val list = mutableListOf<Entity>()
            val limit = selector.appendLimit
            if (selector.box != null) {
                mc.world!!.collectEntitiesByType(selector.entityFilter, selector.box!!.offset(pos), predicate, list, limit)
            } else {
                mc.world!!.entityLookup.forEach(selector.entityFilter) { entity ->
                    if (predicate.test(entity)) {
                        list.add(entity)
                        if (list.size >= limit) {
                            return@forEach LazyIterationConsumer.NextIteration.ABORT
                        }
                    }
                    LazyIterationConsumer.NextIteration.CONTINUE
                }
            }
            if (list.size > 1) {
                selector.sorter.accept(pos, list)
            }
            return list.subList(0, min(selector.limit, list.size))
        }
    }
}

fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    fun clearAll(@Suppress("UNUSED_PARAMETER") context: Any): Int {
        val client = MinecraftClient.getInstance()
        var count = 0
        for (entity in client.world!!.entities) {
            entity.setFlag(6, false)
            count++
        }
        return count
    }
    ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client ->
        if (client.player != null) {
            selected(client.player!!.pos, client.player)
                .forEach(Consumer { it.setFlag(6, true) })
        }
    })
    dispatcher.register(ClientCommandManager.literal("glow")
        .then(ClientCommandManager.literal("clear").executes(::clearAll))
        .then(ClientCommandManager.argument("entities", EntityArgumentType.entities())
            .executes { context ->
                selector = context.getArgument("entities", EntitySelector::class.java)
                clearAll(context)
                context.source.sendFeedback(Text.translatable("reden.commands.glow.success"))
                1
            })
    )
}
