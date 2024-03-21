package com.github.zly2006.reden.mixin.richTranslation;

import com.github.zly2006.reden.utils.richTranslation.RichTranslationKt;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

// overwrite owo
@Mixin(value = TranslatableTextContent.class, priority = 10)
public class MixinTranslatable {
    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private Object[] args;

    @Shadow
    private List<StringVisitable> translations;

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
        var text = RichTranslationKt.processTranslate(language, key, args);
        if (text != null) {
            translations = Collections.singletonList(text);
            ci.cancel();
        }
    }
}
