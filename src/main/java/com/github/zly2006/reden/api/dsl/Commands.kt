package com.github.zly2006.reden.api.dsl

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.text.Text

@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@DslMarker
annotation class CommandBuilder

@CommandBuilder
class BuilderScope<S>(
    val node: ArgumentBuilder<S, *>,
    var builders: MutableList<ArgumentBuilder<S, *>>? = mutableListOf()
) {
    @CommandBuilder
    fun literal(name: String) = LiteralArgumentBuilder.literal<S>(name)!!
        .also(requireNotNull(builders) { "Command already built" }::add)

    @CommandBuilder
    fun <T> argument(name: String, type: ArgumentType<T>) = RequiredArgumentBuilder.argument<S, T>(name, type)!!
        .also(requireNotNull(builders) { "Command already built" }::add)

    @CommandBuilder
    fun optional(builder: ArgumentBuilder<S, *>, function: BuilderScope<S>.() -> Unit) {
        builder.then(function)
        this.function()
        node.then(builder)
    }
}

@CommandBuilder
inline fun <S> ArgumentBuilder<S, *>.then(function: (BuilderScope<S>).() -> Unit) = apply {
    val scope = BuilderScope(this)
    function(scope)
    scope.builders!!.forEach(this::then)
    scope.builders = null
}

operator fun <S> ArgumentBuilder<S, *>.invoke(function: BuilderScope<S>.() -> Unit) = then(function)

@Suppress("NOTHING_TO_INLINE")
inline fun error(reason: String): Nothing =
    throw SimpleCommandExceptionType(Text.literal(reason)).create()
