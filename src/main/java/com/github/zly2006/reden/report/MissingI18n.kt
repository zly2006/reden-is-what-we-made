package com.github.zly2006.reden.report

import com.github.zly2006.reden.utils.isDebug
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import java.io.File

private val missing = hashSetOf<String>()

fun setupMissingI18n(definitions: MutableList<String>) {
    ClientLifecycleEvents.CLIENT_STOPPING.register {
        if (missing.isNotEmpty() && isDebug) {
            File("reden_missing_i18n-${definitions.joinToString("-")}.txt").writeText(missing.joinToString("\n"))
        }
    }
}

fun addMissingI18n(key: String) {
    if (isDebug) missing.add(key)
}
