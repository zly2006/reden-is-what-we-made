package com.github.zly2006.reden.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class RedenCarpetSettings {
    private static final String CATEGORY_REDEN = "Reden";
    private static final String CATEGORY_DEBUGGER = "Reden-Debugger";
    /**
     * In 1.18-, the stack overflow error will be thrown when there are too many updates in one tick.
     * However, we have to replace the recursive method with a loop to use breakpoint features.
     * This is the of updates in one tick,
     * when this number is reached and the game is 1.18-, we throw a stack overflow error.
     */
    @Rule(
            categories = {CATEGORY_REDEN, RuleCategory.CREATIVE},
            options = {"-1", "500000"},
            strict = false
    )
    public static int stackOverflowUpdates = 500000;

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
    public static boolean undoScheduledTicks = false;

    @Rule(
            categories = {CATEGORY_REDEN}
    )
    public static boolean redenDebug = false;

    public static class Debugger {
        @Contract(pure = true)
        public static boolean debuggerBlockUpdates() {
            return redenDebuggerEnabled && redenDebuggerBlockUpdates;
        }

        static class Validators extends Validator<Boolean> {
            @Override
            public Boolean validate(@Nullable ServerCommandSource source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
                if (newValue && !redenDebuggerEnabled) {
                    if (source != null) {
                        source.sendMessage(Text.literal("Warning: ").formatted(Formatting.RED)
                                .append(Text.literal("The rule "))
                                .append(Text.literal("redenDebuggerEnabled").styled(style ->
                                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/carpet redenDebuggerEnabled"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("show the rule")))
                                                .withBold(true)
                                                .withColor(Formatting.GOLD)))
                                .append(Text.literal(" is not enabled, so this option will not work.").formatted(Formatting.RED)));
                    }
                }
                return newValue;
            }
        }
    }

    @Rule(
            categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER}
    )
    public static boolean redenDebuggerEnabled = false;

    @Rule(
            categories = {CATEGORY_REDEN, CATEGORY_DEBUGGER},
            validators = Debugger.Validators.class
    )
    public static boolean redenDebuggerBlockUpdates = true;
}
