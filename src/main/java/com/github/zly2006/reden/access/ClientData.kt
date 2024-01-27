package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.wormhole.Wormhole
import com.github.zly2006.reden.Reden.LOGGER;
import net.minecraft.client.MinecraftClient
import net.minecraft.network.NetworkSide
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
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
                .forEach {
                    try {
                        val repo = RvcRepository(Git.open(it), side = NetworkSide.CLIENTBOUND)
                        rvcStructures[it.name] = repo
                    } catch (e: RepositoryNotFoundException) {
                        LOGGER.warn("Dir '" + it.name + "' is not a git repo, it will be deleted")
                        it.deleteRecursively()
                    }
                }
    }

    interface ClientDataAccess {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("getClientData\$reden")
        fun clientData(): ClientData
    }

    companion object {
        @JvmStatic
        val MinecraftClient.data: ClientData get() {
            return (this as ClientDataAccess).clientData()
        }
    }
}