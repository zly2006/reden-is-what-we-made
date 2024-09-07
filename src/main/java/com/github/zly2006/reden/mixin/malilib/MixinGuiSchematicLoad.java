package com.github.zly2006.reden.mixin.malilib;

import com.github.zly2006.reden.minenv.MinenvScreen;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.GuiSchematicLoad;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
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
        var label = "Download from Web";
        int buttonWidth = this.getStringWidth(label) + 10;
        x -= buttonWidth + 4;
        var button = new ButtonGeneric(x, y, buttonWidth, 20, label);
        this.addButton(button, (button1, mouseButton) -> client.setScreen(new MinenvScreen()));
    }
}
