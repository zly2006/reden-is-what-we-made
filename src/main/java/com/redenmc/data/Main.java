package com.redenmc.data;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.util.GlException;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.Proxy;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("s");

    public static boolean join = false;

    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        SharedConstants.enableDataFixerOptimization();
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        String string4 = "Reden Unit Test | MC " + SharedConstants.getGameVersion().getName();
        String string5 = "release";
        File file = new File(".");
        UUID uuid = Uuids.getOfflinePlayerUuid("Dev");
        CrashReport.initCrashReport();
        Bootstrap.initialize();
        GameLoadTimeEvent.INSTANCE.setBootstrapTime(Bootstrap.LOAD_TIME.get());
        Bootstrap.logMissing();
        Util.startTimerHack();
        Session.AccountType accountType = Session.AccountType.LEGACY;

        Session session = new Session(
                "Dev",
                uuid, "RedenMC", Optional.empty(), Optional.empty(), accountType);
        File file3 = new File(file, "resourcepacks/");
        File file2 = new File(file, "assets/");
        RunArgs runArgs = new RunArgs(
                new RunArgs.Network(session, new PropertyMap(), new PropertyMap(), Proxy.NO_PROXY),
                new WindowSettings(854, 480, OptionalInt.empty(), OptionalInt.empty(), false),
                new RunArgs.Directories(file, file3, file2, null),
                new RunArgs.Game(bl2, string4, string5, bl3, bl4),
                new RunArgs.QuickPlay(null, null, null, null)
        );
        Thread thread = new Thread("Client Shutdown Thread") {
            public void run() {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient != null) {
                    IntegratedServer integratedServer = minecraftClient.getServer();
                    if (integratedServer != null) {
                        integratedServer.stop(true);
                    }

                }
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        Runtime.getRuntime().addShutdownHook(thread);

        final MinecraftClient minecraftClient;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraftClient = new MinecraftClient(runArgs);
            RenderSystem.finishInitialization();
        } catch (GlException var81) {
            LOGGER.warn("Failed to create window: ", var81);
            return;
        } catch (Throwable var82) {
            CrashReport crashReport = CrashReport.create(var82, "Initializing game");
            CrashReportSection crashReportSection = crashReport.addElement("Initialization");
            WinNativeModuleUtil.addDetailTo(crashReportSection);
            MinecraftClient.addSystemDetailsToCrashReport(null, null, runArgs.game.version, null, crashReport);
            MinecraftClient.printCrashReport(null, new File("."), crashReport);
            return;
        }

        Thread thread2;
        if (minecraftClient.shouldRenderAsync()) {
            thread2 = new Thread("Game thread") {
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraftClient.run();
                    } catch (Throwable var2) {
                        LOGGER.error("Exception in client thread", var2);
                    }
                }
            };
            thread2.start();
            if (!join) return;
            while (minecraftClient.isRunning()) {
            }
        } else {
            thread2 = null;

            try {
                RenderSystem.initGameThread(false);
                if (join) minecraftClient.run();
            } catch (Throwable var80) {
                LOGGER.error("Unhandled game exception", var80);
            }
        }

        BufferRenderer.reset();

        if (join) {
            try {
                minecraftClient.scheduleStop();
                if (thread2 != null) {
                    thread2.join();
                }
            } catch (InterruptedException var78) {
                LOGGER.error("Exception during client thread shutdown", var78);
            } finally {
                minecraftClient.stop();
            }
        }
    }
}
