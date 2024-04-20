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
        val timeSum = samplerData.threadsList.sumOf { it.timesList.sum() }
        val redenTime = samplerData.threadsList.sumOf {
            it.childrenList.filter { it.className.contains("reden", true) || it.methodName.contains("reden", true) }
                .sumOf {
                    it.timesCount
                }
        }
        println("${redenTime.toDouble() / timeSum * 100}% of the time is spent in Reden")
        println("Total time: $timeSum ms")
        println("Reden time: $redenTime ms")
    }

    @JvmField
    var output: SparkSamplerProtos.SamplerData? = null
}
