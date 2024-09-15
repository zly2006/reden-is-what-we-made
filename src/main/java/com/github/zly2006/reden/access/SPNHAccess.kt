package com.github.zly2006.reden.access

import net.minecraft.server.network.ServerPlayerEntity

@Suppress("INAPPLICABLE_JVM_NAME")
interface SPNHAccess {
    @get:JvmName("getBehalfPlayer\$reden")
    @set:JvmName("setBehalfPlayer\$reden")
    var behalfPlayer: ServerPlayerEntity?
}
