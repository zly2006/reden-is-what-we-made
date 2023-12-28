package com.github.zly2006.reden.mixin.yo;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.gui.CreditScreen;
import com.github.zly2006.reden.gui.componments.TextureButtonComponent;
import com.github.zly2006.reden.report.ReportKt;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Unique
    TextureButtonComponent icon;

    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void redenInit(CallbackInfo ci) {
        assert client != null;
        icon = new TextureButtonComponent(
                Reden.identifier("reden_16.png"),
                button -> {
                    ReportKt.onFunctionUsed("openCredits_mcTitleScreen");
                    client.setScreen(new CreditScreen(this));
                },
                20,
                20,
                16,
                16,
                Text.of("This is Reden!")
        );
        addDrawableChild(icon);
        icon.setPosition(this.width / 2 + 128, this.height / 4 + 132);
    }
}
