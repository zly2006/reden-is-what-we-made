package com.github.zly2006.reden.mixin.yo;

import com.github.zly2006.reden.report.MissingI18nKt;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TranslationStorage.class)
public class MixinLanguage {
    @Inject(
            method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At("RETURN")
    )
    private static void init(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft, CallbackInfoReturnable<TranslationStorage> cir) {
        MissingI18nKt.setupMissingI18n(definitions);
    }

    @Inject(
            method = "get",
            at = @At("RETURN")
    )
    private void get(String key, String fallback, CallbackInfoReturnable<String> cir) {
        if (key.startsWith("reden.") && cir.getReturnValue().equals(fallback)) {
            MissingI18nKt.addMissingI18n(key);
        }
    }
}
