package com.github.zly2006.reden.rvc.tracking

import java.io.BufferedInputStream

interface IRvcFileReader {
    fun read(content: BufferedInputStream, structure: TrackedStructure)

    data class RvcHeader(
        val metadata: MutableMap<String, String> = mutableMapOf()
    ) {
        constructor(header: String): this() {
            if (!header.startsWith("RVC; ")) {
                throw IllegalArgumentException("Invalid RVC header")
            }
            header.split("; ").drop(1).forEach {
                val key = it.substringBefore(": ")
                val value = it.substringAfter(": ")
                metadata[key] = value
            }
        }

        override fun toString(): String {
            var str = "RVC; "
            metadata.forEach { (key, value) ->
                str += "$key: $value; "
            }
            return str.substring(0, str.length - 2)
        }
    }
}