package com.github.zly2006.reden.utils;

import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;

public class Utils {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public static String blockPropertyName(@NotNull Property property, Comparable value) {
        return property.name(value);
    }
}
