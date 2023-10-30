package com.github.zly2006.reden.mixin.itemShadow;

import com.github.zly2006.reden.access.ItemStackAccess;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.itemShadow.ItemStackOwner;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ItemStack.class)
public class MixinItemStack implements ItemStackAccess {
    @Unique private List<ItemStackOwner> owners;

    @Override
    public @NotNull List<ItemStackOwner> getStackOwners() {
        if (owners == null) {
            if (RedenCarpetSettings.Debugger.debuggerItemShadow()) {
                owners = new ArrayList<>(4);
            } else {
                throw new RuntimeException("reading stack owners without debugger enabled");
            }
        }
        return owners;
    }
}
