package com.github.zly2006.reden.access;

import com.github.zly2006.reden.itemShadow.ItemStackOwner;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ItemStackAccess {
    default void checkStackOwners() {
        getStackOwners().removeIf(it -> !it.checkContains((ItemStack) this));
    }

    List<ItemStackOwner> getStackOwners();
}
