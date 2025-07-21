package com.github.zly2006.reden.mixin.client.chat;

import com.github.zly2006.reden.gui.QuickMenuWidget;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.ChatMixinHelper;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow @Nullable protected abstract Style getComponentStyleAt(double d, double e);
    @Unique
    QuickMenuWidget quickMenuWidget = null;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void keyPressed(Minecraft client, Screen screen) {
        if (screen == null) {
            if (client.screen == reden$getThis()) {
                client.setScreen(null);
            }
        } else {
            client.setScreen(screen);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (quickMenuWidget != null) {
            // highest priority
            if (quickMenuWidget.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) { // Right click
            Minecraft client = Minecraft.getInstance();
            GuiMessage.Line line = reden$geMessageAt(mouseX, mouseY);
            if (line != null && MalilibSettingsKt.CHAT_RIGHT_CLICK_MENU.getBooleanValue()) {
                rightClickMenu((int) mouseX, (int) mouseY, client, line, getComponentStyleAt(mouseX, mouseY));
                cir.setReturnValue(true);
            }
        }
    }

    @Unique private void rightClickMenu(int mouseX, int mouseY, Minecraft client, GuiMessage.Line line, Style style) {
        if (quickMenuWidget != null) {
            quickMenuWidget.remove();
        }
        quickMenuWidget = new QuickMenuWidget(this, mouseX + 1, mouseY + 1) {
            @Override
            public void remove() {
                quickMenuWidget = null;
            }
        };
        ChatMixinHelper.initRightClickMenu(quickMenuWidget, line, style);
    }

    @Unique
    private GuiMessage.Line reden$geMessageAt(double x, double y) {
        ChatComponent chat = Minecraft.getInstance().gui.getChat();
        int i = chat.getMessageLineIndexAt(0, chat.screenToChatY(y));
        if (i >= 0 && i < chat.trimmedMessages.size()) {
            return chat.trimmedMessages.get(i);
        }
        return null;
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z"), cancellable = true)
    private void ct$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ChatScreen.hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                Minecraft.getInstance().gui.getChat().scrollChat(1);
                cir.setReturnValue(true);
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                Minecraft.getInstance().gui.getChat().scrollChat(-1);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (quickMenuWidget != null) {
            quickMenuWidget.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Unique
    private ChatScreen reden$getThis() {
        return (ChatScreen) (Object) this;
    }
}
