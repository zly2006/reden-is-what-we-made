package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.wormhole.Wormhole
import net.minecraft.client.MinecraftClient
import net.minecraft.network.NetworkSide
import org.eclipse.jgit.api.Git
import java.io.File

class ClientData(
    @get:JvmName("mc") val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0
    val breakpoints = BreakpointsManager(true)
    val lastTriggeredBreakpoint: BreakPoint? = null
    val rvcStructures = mutableMapOf<String, RvcRepository>()
    val wormholes = mutableListOf<Wormhole>()

    init {
        File("rvc").mkdirs()
        File("rvc").listFiles()!!.asSequence()
            .filter { it.isDirectory && it.resolve(".git").exists() }
            .map { RvcRepository(Git.open(it), side = NetworkSide.CLIENTBOUND) }
            .forEach { rvcStructures[it.name] = it }
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    interface ClientDataAccess {
        @get:JvmName("getClientData\$reden")
        val clientData: ClientData
    }

    companion object {
        @JvmStatic
        val MinecraftClient.data: ClientData get() {
            return (this as ClientDataAccess).clientData
        }
    }
}
