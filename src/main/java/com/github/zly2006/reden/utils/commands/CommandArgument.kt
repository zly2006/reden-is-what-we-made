package com.github.zly2006.reden.utils.commands

import kotlin.reflect.KProperty

/**
 * Define a set of command arguments in our kotlin dsl.
 *
 * e.g.
 * ```
 * class MyArgs: CommandArguments() {
 *    val name by string("name")
 *    val message by greedyString("message")
 * }
 * ```
 *
 * then, user can use
 * ```
 * context.receiveArgs<MyArgs>()
 * ```
 * to get the arguments.
 */
class CommandArgument {
    // todo
    abstract class MyDelegate<T>(
        private val name: String,
        private val description: String,
        private val required: Boolean = true,
        private val default: T? = null
    ) {
        abstract operator fun getValue(thisRef: CommandArgument, property: KProperty<*>): T
    }
//    val name by string("name")
//    val message by greedyString("message")
}
