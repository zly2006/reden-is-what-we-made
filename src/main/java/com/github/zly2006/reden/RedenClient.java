package com.github.zly2006.reden;

import com.github.zly2006.reden.clientGlow.ClientGlowKt;
import com.github.zly2006.reden.issueTracker.GithubIssue;
import com.github.zly2006.reden.malilib.KeyCallbacksKt;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.malilib.data.CommandHotkey;
import com.github.zly2006.reden.pearl.PearlTask;
import com.github.zly2006.reden.report.ReportKt;
import com.github.zly2006.reden.rvc.RvcLocalCommandKt;
import com.github.zly2006.reden.rvc.gui.RvcHudRenderer;
import com.github.zly2006.reden.rvc.gui.hud.gameplay.SelectModeHudKt;
import com.github.zly2006.reden.rvc.tracking.client.ClientTrackingKt;
import com.github.zly2006.reden.sponsor.LuckToday;
import com.github.zly2006.reden.update.AutoUpdateKt;
import com.github.zly2006.reden.utils.DebugKt;
import com.github.zly2006.reden.utils.UtilsKt;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.util.FileUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RedenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UtilsKt.checkMalilib();
        PearlTask.Companion.register();
        SelectModeHudKt.registerHud();
        InitializationHandler.getInstance().registerInitializationHandler(() -> {
            RenderEventHandler.getInstance().registerGameOverlayRenderer(RvcHudRenderer.INSTANCE);
            ConfigManager.getInstance().registerConfigHandler("reden", new IConfigHandler() {
                @Override
                public void load() {
                    try {
                        File file = new File(FileUtils.getConfigDirectory(), Reden.CONFIG_FILE);
                        if (!file.exists()) {
                            return;
                        }
                        JsonObject jo = Reden.GSON.fromJson(Files.readString(file.toPath()), JsonObject.class);
                        ConfigUtils.readConfigBase(jo, Reden.MOD_NAME, MalilibSettingsKt.getAllOptions());
                        if (DebugKt.isDebug()) {
                            DebugKt.startDebugAppender();
                        }
                    } catch (IOException e) {
                        save();
                    }
                }

                @Override
                public void save() {
                    saveMalilibOptions();
                }
            });
            ClientTrackingKt.registerSelectionTool();
            InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
                @Override
                public void addKeysToMap(IKeybindManager iKeybindManager) {
                    MalilibSettingsKt.HOTKEYS.stream()
                            .map(IHotkey::getKeybind)
                            .forEach(iKeybindManager::addKeybindToMap);

                    for (CommandHotkey commandHotkey : MalilibSettingsKt.RUN_COMMAND.getCommandHotkeyList()) {
                        iKeybindManager.addKeybindToMap(commandHotkey.getKeybind());
                        commandHotkey.getKeybind().setCallback((action, key) -> {
                            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
                            if (networkHandler != null) {
                                for (String command : commandHotkey.getCommands()) {
                                    if (command.startsWith("/")) {
                                        networkHandler.sendChatCommand(command.substring(1));
                                    } else {
                                        networkHandler.sendChatMessage(command);
                                    }
                                }
                                return true;
                            }
                            return false;
                        });
                    }
                }

                @Override
                public void addHotkeys(IKeybindManager iKeybindManager) {
                    iKeybindManager.addHotkeysForCategory("Reden", "reden.hotkeys.category.generic_hotkeys", MalilibSettingsKt.HOTKEYS);
                }
            });
            KeyCallbacksKt.configureKeyCallbacks(MinecraftClient.getInstance());
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(ReportKt::redenSetup);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RvcLocalCommandKt.register(dispatcher);
            ClientGlowKt.register(dispatcher);
            dispatcher.register(ClientCommandManager.literal("qubit").executes(context -> {
                throw new Error("Qu(b)it!");
            }).then(ClientCommandManager.literal("floating-item")
                    .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                client.gameRenderer.showFloatingItem(ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false));
                                client.world.playSound(client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ITEM_TOTEM_USE, client.player.getSoundCategory(), 1.0F, 1.0F, false);
                                return 1;
                            }))).then(ClientCommandManager.literal("luck-today").executes(context -> {
                context.getSource().sendFeedback(
                        Text.literal(String.valueOf(LuckToday.Companion.getLuckValue().getData()))
                );
                return 1;
            })).then(ClientCommandManager.literal("relaunch").executes(context -> {
                AutoUpdateKt.relaunch(null);
                return 1;
            })));
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                GithubIssue issue = new GithubIssue("zly2006", "reden-is-what-we-made", 38);
            } catch (Exception e) {
                Reden.LOGGER.error("", e);
            }
        });
    }

    public static void saveMalilibOptions() {
        JsonObject jo = new JsonObject();
        new File(FileUtils.getConfigDirectory(), "reden").mkdirs();
        ConfigUtils.writeConfigBase(jo, Reden.MOD_NAME, MalilibSettingsKt.getAllOptions());
        try {
            Files.writeString(new File(FileUtils.getConfigDirectory(), Reden.CONFIG_FILE).toPath(), Reden.GSON.toJson(jo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
