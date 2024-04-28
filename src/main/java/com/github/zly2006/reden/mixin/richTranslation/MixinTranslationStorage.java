package com.github.zly2006.reden.mixin.richTranslation;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TranslationStorageAccess;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage implements com.github.zly2006.reden.access.TranslationStorageAccess {
    @Unique
    private final Map<String, Text> textMap = new HashMap<>();
    @Unique
    private static final Map<String, Text> tempTextMap = new HashMap<>();

    @Override
    public @NotNull Map<String, Text> getTextMap$reden() {
        return textMap;
    }

    @Inject(
            method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private static void onWarn(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft, CallbackInfoReturnable<TranslationStorage> cir, @Local Exception e) {
        Reden.LOGGER.error("mc failed to load lang.", e);
    }

    @Inject(
            method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resource/language/TranslationStorage;load(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"
            )
    )
    private static void loadCustomText(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft, CallbackInfoReturnable<TranslationStorage> cir, @Local Identifier identifier) {
        Gson gson = new Gson();
        resourceManager.getAllResources(identifier).forEach(resource -> {
            try {
                var jo = gson.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
                jo.entrySet().stream().filter(it -> it.getValue() instanceof JsonArray).forEach(it -> {
                    MutableText text = Text.Serialization.fromJsonTree(it.getValue());
                    tempTextMap.put(it.getKey(), text);
                });
            } catch (IOException ignored) {
            }
        });
    }

    @Inject(
            method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At("TAIL")
    )
    private static void finish(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft, CallbackInfoReturnable<TranslationStorage> cir) {
        ((TranslationStorageAccess) cir.getReturnValue()).getTextMap$reden().putAll(tempTextMap);
        tempTextMap.clear();
    }
}
