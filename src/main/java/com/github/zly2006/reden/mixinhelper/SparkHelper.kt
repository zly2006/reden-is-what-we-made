package com.github.zly2006.reden.mixinhelper

import me.lucko.spark.common.sampler.Sampler
import net.fabricmc.loader.api.FabricLoader

val sparkInstalled = FabricLoader.getInstance().isModLoaded("spark")

/**
 * Requires [sparkInstalled]
 */
object SparkHelper {
    fun analyze() {
        TODO("Not yet implemented")
    }

    @JvmField
    var sampler: Sampler? = null
}
