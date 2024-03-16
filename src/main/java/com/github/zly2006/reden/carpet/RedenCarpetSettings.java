package com.github.zly2006.reden.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validator;
import com.github.zly2006.reden.utils.DebugKt;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedenCarpetSettings {
    private static final String CATEGORY_REDEN = "Reden";
    private static final String CATEGORY_DEBUGGER = "Reden-Debugger";

    public static class Options {

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                options = {"-1", "0", "52428800"}, // 50 MB
                strict = false
        )
        public static int allowedUndoSizeInBytes = 52428800;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                options = {"0", "100"},
                strict = false
        )
        public static int tickBackMaxTicks = 100;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoScheduledTicks = true;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoEntities = true;

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE}
        )
        public static boolean undoApplyingClearScheduledTicks = true;

        private static class DebugOptionObserver extends Validator<Boolean> {
            @Override
            public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
                if (!UtilsKt.isClient()) {
                    if (newValue) {
                        DebugKt.startDebugAppender();
                    } else {
                        DebugKt.stopDebugAppender();
                    }
                }
                return newValue;
            }
        }

        @Rule(
                categories = {CATEGORY_REDEN},
                validators = {DebugOptionObserver.class}
        )
        public static boolean redenDebug = false;

        @Rule(
                categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER}
        )
        public static boolean redenDebuggerEnabled = false;

        @Rule(
                categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER},
                validators = Debugger.Validators.class
        )
        public static boolean redenDebuggerUpdater = false;

        /**
         Note: this can cause updates lost, disabling it by default.
         Use {@link com.github.zly2006.reden.mixinhelper.RedenNeighborUpdater}
         and {@link #redenDebuggerUpdater} instead
         */
        @Rule(
                categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER},
                validators = Debugger.Validators.class
        )
        public static boolean redenDebuggerBlockUpdates = false;
        @Rule(
                categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER},
                validators = Debugger.Validators.class
        )
        public static boolean redenDebuggerItemShadow = true;

        private static class InvisibleShadowingItemsValidator extends Validator<Boolean> {
            @Override
            public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
                if (newValue && source != null) {
                    source.getServer().getWorlds().forEach(world ->
                            world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), entity -> true).forEach(item -> {
                                System.out.println(item.getStack());
                                PlayerManager playerManager = source.getServer().getPlayerManager();
                                playerManager.sendToDimension(
                                        item.createSpawnPacket(),
                                        world.getRegistryKey()
                                );
                                playerManager.sendToDimension(new EntityTrackerUpdateS2CPacket(
                                        item.getId(),
                                        List.of(new DataTracker.SerializedEntry<>(
                                                ItemEntity.STACK.getId(),
                                                ItemEntity.STACK.getType(),
                                                ItemEntity.STACK.getType().copy(item.getStack())
                                        ))
                                ), world.getRegistryKey());
                            }));

                }
                return newValue;
            }
        }

        @Rule(
                categories = {CATEGORY_REDEN, RuleCategory.CREATIVE},
                validators = {InvisibleShadowingItemsValidator.class}
        )
        public static boolean fixInvisibleShadowingItems = false;

        @Rule(
                categories = RuleCategory.CREATIVE
        )
        public static boolean realFakePlayer = false;

        @Rule(
                categories = RuleCategory.CREATIVE
        )
        public static int modifyStructureBlockDetectRange = -1;
    }


    public static class Debugger {
        @Contract(pure = true)
        public static boolean debuggerBlockUpdates() {
            return Options.redenDebuggerEnabled && Options.redenDebuggerUpdater;
        }

        @Contract(pure = true)
        public static boolean debuggerItemShadow() {
            return Options.redenDebuggerEnabled && Options.redenDebuggerItemShadow;
        }

        static class Validators extends Validator<Boolean> {
            @Override
            public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
                if (newValue && !Options.redenDebuggerEnabled) {
                    if (source != null) {
                        source.sendMessage(Text.translatable("reden.Carpet.warning" + ": ").formatted(Formatting.RED)
                                .append(Text.translatable("reden.Carpet.rule"))
                                .append(Text.translatable("reden.Carpet.redenDebuggerEnabled").styled(style ->
                                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/carpet redenDebuggerEnabled"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("show the rule")))
                                                .withBold(true)
                                                .withColor(Formatting.GOLD)))
                                .append(Text.translatable("reden.Carpet.TextTip").formatted(Formatting.RED)));
                    }
                }
                return newValue;
            }
        }
    }
}
