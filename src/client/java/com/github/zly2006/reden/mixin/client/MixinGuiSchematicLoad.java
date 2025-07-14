package com.github.zly2006.reden.mixin.client;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.webmatic.MevScreen;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.GuiSchematicLoad;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiSchematicLoad.class, remap = false)
public abstract class MixinGuiSchematicLoad extends GuiSchematicBrowserBase {
    public MixinGuiSchematicLoad(int browserX, int browserY) {
        super(browserX, browserY);
    }

    @Inject(
            method = "initGui",
            remap = false,
            at = @At(value = "TAIL")
    )
    private void addButton(CallbackInfo ci, @Local(ordinal = 0) int x, @Local(ordinal = 1) int y) {
        try {
            var label = "Download";
            int buttonWidth = this.getStringWidth(label) + 10;
            x -= buttonWidth + 4;
            var button = new ButtonGeneric(x, y, buttonWidth, 20, label);
            @SuppressWarnings("unchecked")
            Class<Screen> clazz = (Class<Screen>) Class.forName("com.github.zly2006.reden.webmatic.MevScreen");
            this.addButton(button, (button1, mouseButton) -> {
                try {
                    mc.setScreen(clazz.getConstructor().newInstance());
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
