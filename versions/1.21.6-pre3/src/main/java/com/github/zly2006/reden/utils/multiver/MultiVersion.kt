package com.github.zly2006.reden.utils.multiver

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.util.ProblemReporter
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput

object Text {
    fun literal(text: String) = Component.literal(text)
    fun of(text: String?) = Component.nullToEmpty(text)
    fun translatable(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }
}

fun Player.sendSystemMessage(text: Component) {
    displayClientMessage(text, false)
}

fun Entity.saveWithoutId(nbt: CompoundTag): CompoundTag {
    val vo = TagValueOutput.createWithContext(
        ProblemReporter.DISCARDING,
        level().registryAccess()
    )
    saveWithoutId(vo)
    return vo.buildResult()
}

fun Entity.load(nbt: CompoundTag) {
    val vi = TagValueInput.create(
        ProblemReporter.DISCARDING,
        level().registryAccess(),
        nbt
    )
    load(vi)
}
