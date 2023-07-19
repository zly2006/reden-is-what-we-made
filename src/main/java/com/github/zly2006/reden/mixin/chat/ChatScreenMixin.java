package com.github.zly2006.reden.mixin.chat;

import com.github.zly2006.reden.access.VisibleChatHudLineAccess;
import com.github.zly2006.reden.gui.QuickMenuWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique QuickMenuWidget quickMenuWidget;
    private static final Pattern urlPattern = Pattern.compile("(https?://)?[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,8}(/\\S*)?");

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Shadow @Nullable protected abstract Style getTextStyleAt(double x, double y);

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void ct$keyPressed(MinecraftClient client, Screen screen) {
        if (screen == null) {
            if (client.currentScreen == ct$getThis()) {
                client.setScreen(null);
            }
        } else {
            client.setScreen(screen);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ct$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (quickMenuWidget != null) {
            // highest priority
            if (quickMenuWidget.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) { // Right click
            MinecraftClient client = MinecraftClient.getInstance();
            ChatHudLine.Visible visible = ct$geMessageAt(mouseX, mouseY);
            if (visible != null) {
                Text text = ((VisibleChatHudLineAccess) (Object) visible).reden$getText();
                if (text != null) {
                    rightClickMenu((int) mouseX, (int) mouseY, client, text);
                    cir.setReturnValue(true);
                }
            }
        }
    }

    private void rightClickMenu(int mouseX, int mouseY, MinecraftClient client, Text text) {
        if (quickMenuWidget != null) {
            quickMenuWidget.remove();
        }
        quickMenuWidget = new QuickMenuWidget(this, mouseX, mouseY);
        String message = text.getString();
        Matcher matcher = urlPattern.matcher(message);
        Style style = getTextStyleAt(mouseX, mouseY);
        if (matcher.find()) {
            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            String finalUrl = url;
            quickMenuWidget.addEntry(Text.literal("Copy URL"), (entry, button) -> {
                client.keyboard.setClipboard(finalUrl);
                entry.setName(Text.literal("Copied"));
            });
        }
        quickMenuWidget.addEntry(Text.literal("Copy Raw"), (entry, button) -> {
            client.keyboard.setClipboard(Text.Serializer.toJson(text));
            entry.setName(Text.literal("Copied"));
        });
        quickMenuWidget.addEntry(Text.literal("Copy"), (entry, button) -> {
            client.keyboard.setClipboard(message);
            entry.setName(Text.literal("Copied"));
        });
        if (style != null) {
            if (style.getHoverEvent() != null) {
                HoverEvent.Action<?> action = style.getHoverEvent().getAction();
                if (action == HoverEvent.Action.SHOW_TEXT) {
                    quickMenuWidget.addEntry(Text.literal("Copy Hover Raw"), (entry, button) -> {
                        Text hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                        client.keyboard.setClipboard(Text.Serializer.toJson(hoverText));
                        entry.setName(Text.literal("Copied"));
                    });
                }
                if (action == HoverEvent.Action.SHOW_ITEM) {
                    quickMenuWidget.addEntry(Text.literal("Give Hover Item"), (entry, button) -> {
                        ItemStack stack = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ITEM).asStack();
                        if (stack.getNbt() == null) {
                            client.getNetworkHandler().sendChatCommand(
                                "give @s " + Registries.ITEM.getId(stack.getItem())
                            );
                        } else {
                            client.getNetworkHandler().sendChatCommand(
                                "give @s " + Registries.ITEM.getId(stack.getItem()) + stack.getNbt().toString()
                            );
                        }
                        entry.setName(Text.literal("Done"));
                    });
                }
                if (action == HoverEvent.Action.SHOW_ENTITY) {
                    quickMenuWidget.addEntry(Text.literal("Copy Hover UUID"), (entry, button) -> {
                        UUID uuid = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY).uuid;
                        client.keyboard.setClipboard(uuid.toString());
                        entry.setName(Text.literal("Copied"));
                    });
                }
            }
            if (style.getClickEvent() != null) {
                if (style.getClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) {
                    quickMenuWidget.addEntry(Text.literal("Copy Click Command"), (entry, button) -> {
                        String command = style.getClickEvent().getValue();
                        client.keyboard.setClipboard(command);
                        entry.setName(Text.literal("Copied"));
                    });
                }
                if (style.getClickEvent().getAction() == ClickEvent.Action.OPEN_FILE) {
                    quickMenuWidget.addEntry(Text.literal("Copy Click File"), (entry, button) -> {
                        String file = style.getClickEvent().getValue();
                        client.keyboard.setClipboard(file);
                        entry.setName(Text.literal("Copied"));
                    });
                }
            }
        }
        addDrawable(quickMenuWidget);
    }

    private ChatHudLine.Visible ct$geMessageAt(double x, double y) {
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        double d = chatHud.toChatLineX(x);
        int i = chatHud.getMessageLineIndex(d, chatHud.toChatLineY(y));
        if (i >= 0 && i < chatHud.visibleMessages.size()) {
            return chatHud.visibleMessages.get(i);
        }
        return null;
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"), cancellable = true)
    private void ct$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ChatScreen.hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                MinecraftClient.getInstance().inGameHud.getChatHud().scroll(1);
                cir.setReturnValue(true);
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                MinecraftClient.getInstance().inGameHud.getChatHud().scroll(-1);
                cir.setReturnValue(true);
            }
        }
    }

    private ChatScreen ct$getThis() {
        return (ChatScreen) (Object) this;
    }
}
