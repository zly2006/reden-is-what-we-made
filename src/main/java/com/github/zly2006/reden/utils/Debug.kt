package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.DEBUG_LOGGER

private val LOGGER = Reden.LOGGER
@JvmField
var debugLogger: (String) -> Unit = { if (isDebug) LOGGER.debug(it) }
val isDebug: Boolean get() = if (isClient) DEBUG_LOGGER.booleanValue else RedenCarpetSettings.redenDebug
