package com.github.zly2006.reden;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.github.zly2006.reden.access.BlockEntityInterface;
import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.fakePlayer.FakeConnection;
import com.github.zly2006.reden.fakePlayer.RedenFakePlayer;
import com.github.zly2006.reden.network.ChannelsKt;
import com.github.zly2006.reden.transformers.ThisIsReden;
import com.github.zly2006.reden.utils.ResourceLoader;
import com.github.zly2006.reden.utils.TaskScheduler;
import com.github.zly2006.reden.utils.UtilsKt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class Reden implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "reden";
    public static final String MOD_NAME = "Reden";
    public static final String CONFIG_FILE = "reden.json";
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final Version MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int REDEN_HIGHEST_MIXIN_PRIORITY = 10;

    @Override
    public String version() {
        return "reden";
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(RedenCarpetSettings.Options.class);
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
            boolean isDev = false;
            // Debug command
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                dispatcher.register(CommandManager.literal("fake-player")
                        .then(CommandManager.literal("spawn")
                                .then(CommandManager.argument("name", StringArgumentType.word())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            UUID uuid = Uuids.getOfflinePlayerUuid(name);
                                            RedenFakePlayer fakePlayer = RedenFakePlayer.create(
                                                    context.getSource().getServer(),
                                                    new GameProfile(uuid, name),
                                                    true
                                            );
                                            FakeConnection fakeConnection = new FakeConnection();
                                            fakeConnection.register(context.getSource().getServer().getNetworkIo());
                                            context.getSource().getServer().getPlayerManager().onPlayerConnect(
                                                    fakeConnection,
                                                    fakePlayer
                                            );
                                            return 1;
                                        })
                                )));
                dispatcher.register(CommandManager.literal("reden-debug")
                                .then(CommandManager.literal("top-undo").executes(context -> {
                                    PlayerData.Companion.data(context.getSource().getPlayer()).topUndo();
                                    return 1;
                                }))
                                .then(CommandManager.literal("top-redo").executes(context -> {
                                    PlayerData.Companion.data(context.getSource().getPlayer()).topRedo();
                                    return 1;
                                }))
                        .then(CommandManager.literal("last-saved-nbt")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(pos);
                                            if (blockEntity == null) {
                                                context.getSource().sendError(Text.of("No block entity at " + pos.toShortString()));
                                                return 0;
                                            }
                                            NbtCompound lastSavedNbt = ((BlockEntityInterface) blockEntity).getLastSavedNbt$reden();
                                            if (lastSavedNbt == null) {
                                                context.getSource().sendError(Text.of("No last saved NBT at " + pos.toShortString()));
                                                return 0;
                                            }
                                            context.getSource().sendMessage(Text.of(lastSavedNbt.toString()));
                                            return 1;
                                        })))
                        .then(CommandManager.literal("shadow-item")
                                .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(access))
                                        .executes(context -> {
                                            ItemStackArgument itemStackArgument = ItemStackArgumentType.getItemStackArgument(context, "item");
                                            ItemStack stack = itemStackArgument.createStack(1, true);
                                            PlayerInventory inventory = context.getSource().getPlayer().getInventory();
                                            for (int i = 0; i < 2; i++) {
                                                int emptySlot = inventory.getEmptySlot();
                                                inventory.setStack(emptySlot, stack);
                                            }
                                            context.getSource().getPlayer().currentScreenHandler.syncState();
                                            return 1;
                                        })))
                        .then(CommandManager.literal("totem-of-undying").executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, EntityStatuses.USE_TOTEM_OF_UNDYING));
                            }
                            return 1;
                        }))
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
            if (!(dispatcher instanceof ThisIsReden)) {
                throw new RuntimeException("This is not Reden!");
            } else {
                LOGGER.info("This is Reden!");
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(TaskScheduler.INSTANCE);
    }

    @Contract("_ -> new")
    public static @NotNull Identifier identifier(@NotNull String id) {
        return new Identifier(MOD_ID, id);
    }
}
