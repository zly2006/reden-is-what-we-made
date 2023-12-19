package com.github.zly2006.reden.debugger

import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

object Translations {
    val Overworld = Text.translatable("reden.constants.world.overworld")
    val Nether = Text.translatable("reden.constants.world.the_nether")
    val End = Text.translatable("reden.constants.world.the_end")
}

fun RegistryKey<World>.translatable() =
    Text.translatable("reden.constants.world.${value.path}")!!

fun BlockEntityType<*>.translatable() =
    Text.translatable("reden.constants.block_entity.${registryEntry?.key?.getOrNull()?.value?.path}")!!
