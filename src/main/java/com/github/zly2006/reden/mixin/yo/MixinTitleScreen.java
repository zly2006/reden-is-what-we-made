package com.github.zly2006.reden.mixin.yo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
        title.asOrderedText().accept((s, a, v) -> {
            System.out.println(((char) s));
            return true;
        });
    }

    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    private void redenInit(CallbackInfo ci) {
    }
}
