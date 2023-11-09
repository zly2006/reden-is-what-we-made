package com.github.zly2006.reden.mixin.privacy;

import com.github.zly2006.reden.gui.PrivacyScreen;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// For some reason idk, i18n is not initialized when the game starts, so we have to wait for it to be initialized
@Mixin(LanguageManager.class)
public class MixinLanguageManager {
    @Inject(
            method = "reload",
            at = @At("RETURN")
    )
    private void reloadPrivacyLanguage(ResourceManager manager, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!MalilibSettingsKt.iPRIVACY_SETTING_SHOWN.getBooleanValue()) {
            client.setScreen(new PrivacyScreen(client.currentScreen));
        }
    }
}
