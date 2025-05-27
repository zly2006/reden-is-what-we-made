package com.github.zly2006.reden.utils;

import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public static String blockPropertyName(@NotNull Property property, Comparable value) {
        return property.name(value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    public static Object blockPropertyValue(@NotNull Property property, String name) {
        return property.parse(name).orElse(null);
    }
}
