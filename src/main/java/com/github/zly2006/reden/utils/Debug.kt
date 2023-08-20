package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden

private val LOGGER = Reden.LOGGER
@JvmField
var debugLogger: (String) -> Unit = { LOGGER.debug(it) }
