package com.github.zly2006.reden.mixin.richTranslation;

import com.github.zly2006.reden.access.TranslationStorageAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// overwrite owo
@Mixin(value = TranslatableTextContent.class, priority = 10)
public class MixinTranslatable {
    @Shadow
    @Final
    private String key;

    @Inject(
            method = "updateTranslations",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/text/TranslatableTextContent;languageCache:Lnet/minecraft/util/Language;",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void translate(CallbackInfo ci, @Local Language language) {
        if (language.get(key) == null) {
            var text = ((TranslationStorageAccess) language).getTextMap$reden().get(key).copy();
            if (text != null) {
                ci.cancel();
            }
        }
    }
}
