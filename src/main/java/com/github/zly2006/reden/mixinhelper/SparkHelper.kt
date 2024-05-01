package com.github.zly2006.reden.mixinhelper

import me.lucko.spark.proto.SparkSamplerProtos
import net.fabricmc.loader.api.FabricLoader

val sparkInstalled = FabricLoader.getInstance().isModLoaded("spark")

/**
 * Requires [sparkInstalled]
 */
object SparkHelper {
    fun analyze() {
        val samplerData = requireNotNull(output) {
            "No report found, use /spark profiler start to start a sampler"
        }
        val thread = samplerData.threadsList[0]
        val timeSum = thread.timesList.sum()
        val redens = thread.childrenList.filter {
                it.className.contains("reden", true) ||
                        it.methodName.contains("reden", true)
        }.toMutableList()
        var index = 0
        while (index < redens.size) {
            val node = redens[index]
            var depth = 0
            var children = listOf(node)
            while (depth < 5 || children.size < 100) {
                val newChildren = children.flatMap { it.childrenRefsList.map { ref -> thread.childrenList[ref] } }
                if (newChildren.isEmpty()) {
                    break
                }
                children = newChildren
                redens.removeAll { it in children }
                depth++
            }
            index++
        }


        val redenTime = redens.sumOf { it.timesList.sum() }
        println("${redenTime / timeSum * 100}% of the time is spent in Reden")
        println("Total time: $timeSum ms")
        println("Reden time: $redenTime ms")
    }

    @JvmField
    var output: SparkSamplerProtos.SamplerData? = null
}
