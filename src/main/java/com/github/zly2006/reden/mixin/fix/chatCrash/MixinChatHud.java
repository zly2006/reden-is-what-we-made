package com.github.zly2006.reden.mixin.fix.chatCrash;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(ChatHud.class)
public class MixinChatHud {
    @Mutable @Shadow @Final public List<ChatHudLine.Visible> visibleMessages;

    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At(
                    value = "HEAD"
            )
    )
    public void beforeAdd(CallbackInfo ci) {
    }
    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At(
                    value = "RETURN"
            )
    )
    public void afterAdd(CallbackInfo ci) {
    }

    @Redirect(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;remove(I)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    public Object onRemove(List<ChatHudLine.Visible> instance, int i) {
        /*if (i >= 100) {
            return null;
        }*/
        return instance.remove(i);
    }


    @Inject(
            method = "<init>",
            at = @At(
                    value = "RETURN"
            )
    )
    public void onInit(CallbackInfo ci) {
        visibleMessages = new LinkedList<>();
    }
}
