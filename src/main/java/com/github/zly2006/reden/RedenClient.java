package com.github.zly2006.reden;

import com.github.zly2006.reden.malilib.KeyCallbacksKt;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.malilib.data.CommandHotkey;
import com.github.zly2006.reden.pearl.PearlTask;
import com.github.zly2006.reden.render.BlockBorder;
import com.github.zly2006.reden.report.ReportKt;
import com.github.zly2006.reden.sponsor.SponsorKt;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import fi.dy.masa.malilib.util.FileUtils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RedenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new Thread(() -> {
            // Http IOs
            ReportKt.initReport();
            SponsorKt.updateSponsors();
        }, "Report Worker").start();
        PearlTask.Companion.register();
        InitializationHandler.getInstance().registerInitializationHandler(() -> {
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
                    } catch (IOException e) {
                        save();
                    }
                }

                @Override
                public void save() {
                    JsonObject jo = new JsonObject();
                    ConfigUtils.writeConfigBase(jo, Reden.MOD_NAME, MalilibSettingsKt.getAllOptions());
                    try {
                        Files.writeString(new File(FileUtils.getConfigDirectory(), Reden.CONFIG_FILE).toPath(), Reden.GSON.toJson(jo));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            InputEventHandler.getInputManager().registerMouseInputHandler(new IMouseInputHandler() {
                @Override
                public boolean onMouseClick(int mouseX, int mouseY, int eventButton, boolean eventButtonState) {
                    if (!eventButtonState) return false;
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                        return false;
                    }
                    ItemStack stack = mc.player.getStackInHand(Hand.MAIN_HAND);
                    if (stack != null && Registries.ITEM.getId(stack.getItem()).equals(Identifier.tryParse(MalilibSettingsKt.SELECTION_TOOL.getStringValue()))) {
                        // get clicked block
                        HitResult raycast = mc.cameraEntity.raycast(256, 0, false);
                        if (raycast.getType() == HitResult.Type.BLOCK) {
                            var blockResult = (BlockHitResult) raycast;
                            mc.player.sendMessage(Text.literal("Clicked block: ").append(Text.translatable(mc.world.getBlockState(blockResult.getBlockPos()).getBlock().getTranslationKey())));
                            BlockBorder.set(blockResult.getBlockPos(), 1);
                        }
                        else {
                            mc.player.sendMessage(Text.literal("Clicked nothing"));
                        }
                        return true;
                    }
                    return false;
                }
            });
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
                            assert networkHandler != null;
                            for (String command : commandHotkey.getCommands()) {
                                if (command.startsWith("/")) {
                                    networkHandler.sendChatCommand(command.substring(1));
                                } else {
                                    networkHandler.sendChatMessage(command);
                                }
                            }
                            return true;
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
        /*DebugKt.debugLogger = str -> {
            if (MinecraftClient.getInstance().player != null && MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue()) {
                MinecraftClient.getInstance().player.sendMessage(Text.of(str));
            }
            return Unit.INSTANCE;
        };*/
    }
}
