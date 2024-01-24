package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.DEBUG
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy
import org.apache.logging.log4j.core.filter.AbstractFilter
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.message.Message
import java.time.Instant

private val LOGGER = Reden.LOGGER
@JvmField
var debugLogger: (String) -> Unit = { if (isDebug) LOGGER.debug(it) }
val isDebug: Boolean get() = if (isClient) DEBUG_LOGGER.booleanValue else RedenCarpetSettings.Options.redenDebug

val isDevVersion: Boolean = listOf("dev", "alpha", "beta").any { Reden.MOD_VERSION.friendlyString.contains(it) }

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
    .setFilter(object : AbstractFilter() {
        fun filterLoggers(loggerName: String): Filter.Result {
            if (loggerName.contains("FabricRegistrySync")) return Filter.Result.DENY // useless
            if (loggerName.contains("Fabric")) return Filter.Result.DENY // deny fabric api debug

            return Filter.Result.NEUTRAL
        }

        override fun filter(logger: Logger, level: Level, marker: Marker?, msg: Message?, t: Throwable?): Filter.Result {
            return filterLoggers(logger.name)
        }

        override fun filter(logger: Logger, level: Level, marker: Marker?, msg: Any?, t: Throwable?): Filter.Result {
            return filterLoggers(logger.name)
        }

        override fun filter(event: LogEvent): Filter.Result {
            if (event.message.formattedMessage.contains("carpet:hello")) return Filter.Result.DENY // WTF packet is this?
            return filterLoggers(event.loggerName)
        }

        override fun filter(logger: Logger, level: Level, marker: Marker, msg: String, vararg params: Any?): Filter.Result {
            if (msg.contains("carpet:hello")) return Filter.Result.DENY // WTF packet is this?
            return filterLoggers(logger.name)
        }
    })
    .build()
    .apply { start() }

fun startDebugAppender() {
    val ctx = LogManager.getContext(false) as LoggerContext
    ctx.configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).level = DEBUG
    ctx.configuration.getLoggerConfig(Reden.MOD_NAME).addAppender(debugAppender, DEBUG, null)
    ctx.updateLoggers()
}

fun stopDebugAppender() {
    val ctx = LogManager.getContext(false) as LoggerContext
    ctx.configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).level = INFO
    ctx.configuration.getLoggerConfig(Reden.MOD_NAME).removeAppender(debugAppender.name)
    ctx.updateLoggers()
}

fun pauseHere(exception: Throwable? = null) {
    val now = Instant.now()
    if (exception == null) {
        Reden.LOGGER.error("Paused.")
    } else {
        Reden.LOGGER.error("Paused because ", exception)
    }
    if (Instant.now().toEpochMilli() - now.toEpochMilli() < 300) {
        Reden.LOGGER.error("Did u forget to place a breakpoint here?")
        Thread.sleep(3000)
    }
}
