package com.github.zly2006.reden.mixin.client.chat;

import com.github.zly2006.reden.access.VisibleChatHudLineAccess;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @Unique private Component currentMessage;

    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD")
    )
    private void addMessage(Component message, MessageSignature messageSignature, GuiMessageTag guiMessageTag, CallbackInfo ci) {
        currentMessage = message;
    }

    @ModifyArg(
        method = "addMessageToDisplayQueue",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0),
        index = 1
    )
    private Object addVisibleMessage(Object element) {
        GuiMessage.Line visible = (GuiMessage.Line) element;
        ((VisibleChatHudLineAccess) element).setText$reden(currentMessage);
        return visible;
    }
}
