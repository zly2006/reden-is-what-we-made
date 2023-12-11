package com.github.zly2006.reden.mixin.fuckMojang;

import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(DirectConnectScreen.class)
public class MixinDirectConnectScreen {
    @Shadow private TextFieldWidget addressField;

    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void fillIllegal(CallbackInfo ci) throws IOException {
    }
}
