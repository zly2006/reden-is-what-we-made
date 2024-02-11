package com.github.zly2006.reden.mixin.survivalProtection;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldListWidget.class)
public interface IMixinWorldListWidget {
    @Invoker("load")
    void invokeLoadForReden();
}