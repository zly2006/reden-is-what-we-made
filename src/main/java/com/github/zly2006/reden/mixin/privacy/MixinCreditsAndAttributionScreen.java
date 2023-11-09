package com.github.zly2006.reden.mixin.privacy;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CreditsAndAttributionScreen.class)
public abstract class MixinCreditsAndAttributionScreen extends Screen {
    protected MixinCreditsAndAttributionScreen(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;refreshPositions()V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void disableCredits(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder) {
    }
}
