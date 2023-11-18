package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy
import org.apache.logging.log4j.core.layout.PatternLayout

private val LOGGER = Reden.LOGGER
@JvmField
var debugLogger: (String) -> Unit = { if (isDebug) LOGGER.debug(it) }
val isDebug: Boolean get() = if (isClient) DEBUG_LOGGER.booleanValue else RedenCarpetSettings.redenDebug

private val debugAppender = RollingRandomAccessFileAppender.Builder()
    .withFileName("logs/reden-debug.log")
    .setLayout(
        PatternLayout.newBuilder()
            .withPattern("[%d{HH:mm:ss}] [%t/%level] (%logger{1}) %msg{nolookups}%n")
            .build()
    )
    .withPolicy(OnStartupTriggeringPolicy.createPolicy(1))
    .withFilePattern("logs/reden-debug-%i.log.gz")
    .setName("RedenDebugAppender")
    .setImmediateFlush(true)
    .build()

fun startDebugAppender() {
    if (!debugAppender.isStarted) {
        debugAppender.start()
        val ctx = LogManager.getContext(false) as LoggerContext
        ctx.configuration.getLoggerConfig(Reden.MOD_NAME)
            .addAppender(debugAppender, Level.DEBUG, null)
        ctx.updateLoggers()
    }
}

fun stopDebugAppender() {
    if (debugAppender.isStarted) {
        debugAppender.stop()
        val ctx = LogManager.getContext(false) as LoggerContext
        ctx.configuration.getLoggerConfig(Reden.MOD_NAME).removeAppender(debugAppender.name)
        ctx.updateLoggers()
    }
}
