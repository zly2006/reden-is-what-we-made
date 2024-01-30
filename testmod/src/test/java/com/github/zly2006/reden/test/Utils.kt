package com.github.zly2006.reden.test

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.datafixer.Schemas
import net.minecraft.resource.VanillaDataPackProvider
import net.minecraft.server.MinecraftServer
import net.minecraft.server.WorldGenerationProgressLogger
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.util.ApiServices
import net.minecraft.world.level.storage.LevelStorage
import java.io.File
import java.net.Proxy

fun setupMinecraftRegistries() {
    SharedConstants.createGameVersion()
    Bootstrap.initialize()
}

fun setupServer() {
    setupMinecraftRegistries()
    val file = File(".")
    val apiServices = ApiServices.create(YggdrasilAuthenticationService(Proxy.NO_PROXY), file)
    val levelStorage = LevelStorage.create(file.toPath())
    val session = levelStorage.createSession("world")
    val resourcePackManager = VanillaDataPackProvider.createManager(session);
    val saveLoader = TODO()
    val serverPropertiesLoader = TODO()
    val server = MinecraftServer.startServer { threadx ->
        val minecraftDedicatedServerxx = MinecraftDedicatedServer(
            threadx, session, resourcePackManager, saveLoader, serverPropertiesLoader, Schemas.getFixer(), apiServices,
            ::WorldGenerationProgressLogger
        )
        minecraftDedicatedServerxx.serverPort = 25565
        minecraftDedicatedServerxx.isDemo = false
        minecraftDedicatedServerxx
    }
}

