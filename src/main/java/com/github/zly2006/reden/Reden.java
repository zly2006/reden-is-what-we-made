package com.github.zly2006.reden;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.network.ChannelsKt;
import com.github.zly2006.reden.rvc.RvcCommandKt;
import com.github.zly2006.reden.utils.ResourceLoader;
import com.github.zly2006.reden.utils.UtilsKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Reden implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "reden";
    public static final String MOD_NAME = "Reden";
    public static final String CONFIG_FILE = "reden.json";
    public static final Version MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion();
    public static final Logger LOGGER = LoggerFactory.getLogger("reden");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String version() {
        return "reden";
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(RedenCarpetSettings.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return ResourceLoader.loadLang(lang);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(UtilsKt::setServer);
        ChannelsKt.register();
        CarpetServer.manageExtension(this);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            // Debug command
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                dispatcher.register(CommandManager.literal("reden-debug")
                        .then(CommandManager.literal("delay-test")
                                .executes(context -> {
                                    try {
                                        Thread.sleep(35 * 1000);
                                    } catch (InterruptedException ignored) {
                                    }
                                    context.getSource().sendMessage(Text.of("35 seconds passed"));
                                    return 1;
                                })));
            }
            RvcCommandKt.register(dispatcher);
        });
    }
}
