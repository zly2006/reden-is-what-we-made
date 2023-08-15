package com.github.zly2006.reden.carpet;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class RedenCarpetSettings {
    private static final String CATEGORY_REDEN = "Reden";
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
}
