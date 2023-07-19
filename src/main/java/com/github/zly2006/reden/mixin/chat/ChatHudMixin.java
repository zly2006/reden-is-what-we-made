package com.github.zly2006.reden.mixin.chat;

import com.github.zly2006.reden.access.VisibleChatHudLineAccess;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Unique private Text currentMessage;

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
        at = @At("HEAD")
    )
    private void ct$addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        currentMessage = message;
    }

    @ModifyArg(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0),
        index = 1
    )
    private Object ct$addVisibleMessage(Object element) {
        ChatHudLine.Visible visible = (ChatHudLine.Visible) element;
        ((VisibleChatHudLineAccess) element).reden$setText(currentMessage);
        return visible;
    }
}
