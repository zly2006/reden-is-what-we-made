package com.github.zly2006.reden.rvc.template

import kotlinx.serialization.json.*

class RvcTemplate(
    val partMapping: Map<String, StructureMapping>,
    val inputConstraints: List<InputConstraint>,
) {
    class StructureMapping(
        val input: String = "default",
        val op: String = "",
        val args: JsonArray,
        val switch: Map<String, String>
    ) {
        fun get(inputs: JsonObject): String {
            return when (op) {
                "mod" -> {
                    val input = inputs[this.input]!!.jsonPrimitive.long
                    val divider = args[0].jsonPrimitive.long
                    val result = (input % divider).toString()
                    switch[result] ?: switch["default"] ?: error("valur not found for $input % $divider")
                }

                else  -> error("Unknown op: $op")
            }
        }
    }

    class InputConstraint(
        val input: String = "default",
        val op: String = "",
        val args: JsonArray
    ) {
        fun check(inputs: JsonObject): Boolean {
            return when (op) {
                "min" -> {
                    val input = inputs[input]!!.jsonPrimitive.long
                    args[0].jsonPrimitive.long <= input
                }

                "max" -> {
                    val input = inputs[input]!!.jsonPrimitive.long
                    args[0].jsonPrimitive.long >= input
                }

                "mod" -> {
                    val input = inputs[input]!!.jsonPrimitive.long
                    input % args[0].jsonPrimitive.long == 0L
                }

                "or"  -> {
                    args.any {
                        val constraint = Json.decodeFromJsonElement<InputConstraint>(it)
                        constraint.check(inputs)
                    }
                }

                "and" -> {
                    args.all {
                        val constraint = Json.decodeFromJsonElement<InputConstraint>(it)
                        constraint.check(inputs)
                    }
                }

                else  -> error("Unknown op: $op")
            }
        }
    }
}
