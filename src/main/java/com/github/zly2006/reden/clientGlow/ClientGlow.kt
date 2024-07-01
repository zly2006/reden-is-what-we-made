package com.github.zly2006.reden.clientGlow

import com.mojang.brigadier.CommandDispatcher
import com.redenmc.bragadier.ktdsl.register
import com.redenmc.bragadier.ktdsl.then
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.function.LazyIterationConsumer
import net.minecraft.util.math.Vec3d
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
        return mc.world!!.players.filter { it.nameForScoreboard == selector.playerName }
    }
    else if (selector.uuid != null) {
        return mc.world!!.entities.filter { it.uuid == selector.uuid }
    }
    else {
        val predicate = selector.getPositionPredicate(selector.positionOffset.apply(pos), null, null)
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

var glowing: Set<Entity> = setOf(); private set

fun registerClientGlow(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client ->
        if (client.player != null) {
            glowing = selected(client.player!!.pos, client.player!!).toSet()
        }
    })
    dispatcher.register {
        literal("glow").then {
            literal("clear").executes {
                selector = null
                glowing = setOf()
                1
            }
            argument("entities", EntityArgumentType.entities()).executes { context ->
                selector = context.getArgument("entities", EntitySelector::class.java)
                context.source.sendFeedback(Text.translatable("reden.commands.glow.success"))
                1
            }
        }
    }
}
