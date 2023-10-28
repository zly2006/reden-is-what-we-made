package com.github.zly2006.reden.mixin;

import com.github.zly2006.reden.RedenClient;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(
            method = "handleTextClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/GameOptions;getChatLinks()Lnet/minecraft/client/option/SimpleOption;"
            ),
            cancellable = true
    )
    private void redenClickEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
        String value = style.getClickEvent().getValue();
        if (value.startsWith("reden:")) {
            String command = value.substring(6);
            if (command.startsWith("malilib:")) {
                String content = command.substring(8);
                String key = content.substring(0, content.indexOf("="));
                String val = content.substring(content.indexOf("=") + 1);
                MalilibSettingsKt.getAllOptions().stream()
                        .filter(it -> it.getName().equals(key))
                        .forEach(it -> it.setValueFromJsonElement(new JsonPrimitive(val)));
                RedenClient.saveMalilibOptions();
            }
            cir.setReturnValue(true);
        }
    }
}
