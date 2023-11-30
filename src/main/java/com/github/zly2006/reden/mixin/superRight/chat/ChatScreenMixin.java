package com.github.zly2006.reden.mixin.superRight.chat;

import com.github.zly2006.reden.access.VisibleChatHudLineAccess;
import com.github.zly2006.reden.gui.QuickMenuWidget;
import com.github.zly2006.reden.intro.SuperRightIntro;
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

import static com.github.zly2006.reden.malilib.MalilibSettingsKt.CHAT_RIGHT_CLICK_MENU;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    private static final Pattern urlPattern = Pattern.compile("(https?://)?[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,8}(/\\S*)?");
    @Unique QuickMenuWidget quickMenuWidget;

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
            if (visible != null && CHAT_RIGHT_CLICK_MENU.getBooleanValue()) {
                Text text = ((VisibleChatHudLineAccess) (Object) visible).reden$getText();
                if (text != null) {
                    rightClickMenu((int) mouseX, (int) mouseY, client, text);
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Unique private void rightClickMenu(int mouseX, int mouseY, MinecraftClient client, Text text) {
        if (quickMenuWidget != null) {
            quickMenuWidget.remove();
        }
        quickMenuWidget = new QuickMenuWidget(this, mouseX + 1, mouseY + 1);
        quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.about"), (e, b) ->
                client.setScreen(new SuperRightIntro()));
        String message = text.getString();
        Matcher matcher = urlPattern.matcher(message);
        Style style = getTextStyleAt(mouseX, mouseY);
        if (matcher.find()) {
            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            String finalUrl = url;
            quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_url"), (entry, button) -> {
                client.keyboard.setClipboard(finalUrl);
                entry.setName(Text.translatable("reden.widget.chat.copied"));
            });
        }
        quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_raw"), (entry, button) -> {
            client.keyboard.setClipboard(Text.Serializer.toJson(text));
            entry.setName(Text.translatable("reden.widget.chat.copied"));
        });
        quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy"), (entry, button) -> {
            client.keyboard.setClipboard(message);
            entry.setName(Text.translatable("reden.widget.chat.copied"));
        });
        if (style != null) {
            if (style.getHoverEvent() != null) {
                HoverEvent.Action<?> action = style.getHoverEvent().getAction();
                if (action == HoverEvent.Action.SHOW_TEXT) {
                    quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_hover_raw"), (entry, button) -> {
                        Text hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                        client.keyboard.setClipboard(Text.Serializer.toJson(hoverText));
                        entry.setName(Text.translatable("reden.widget.chat.copied"));
                    });
                }
                if (action == HoverEvent.Action.SHOW_ITEM) {
                    quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.give_hover_item"), (entry, button) -> {
                        @SuppressWarnings("DataFlowIssue")
                        ItemStack stack = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ITEM).asStack();
                        if (stack.getNbt() == null) {
                            //noinspection DataFlowIssue
                            client.getNetworkHandler().sendChatCommand(
                                "give @s " + Registries.ITEM.getId(stack.getItem())
                            );
                        } else {
                            //noinspection DataFlowIssue
                            client.getNetworkHandler().sendChatCommand(
                                "give @s " + Registries.ITEM.getId(stack.getItem()) + stack.getNbt().toString()
                            );
                        }
                        entry.setName(Text.translatable("reden.widget.chat.done"));
                    });
                }
                if (action == HoverEvent.Action.SHOW_ENTITY) {
                    quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_hover_uuid"), (entry, button) -> {
                        UUID uuid = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY).uuid;
                        client.keyboard.setClipboard(uuid.toString());
                        entry.setName(Text.translatable("reden.widget.chat.copied"));
                    });
                }
            }
            if (style.getClickEvent() != null) {
                if (style.getClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) {
                    quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_click_command"), (entry, button) -> {
                        String command = style.getClickEvent().getValue();
                        client.keyboard.setClipboard(command);
                        entry.setName(Text.translatable("reden.widget.chat.copied"));
                    });
                }
                if (style.getClickEvent().getAction() == ClickEvent.Action.OPEN_FILE) {
                    quickMenuWidget.addEntry(Text.translatable("reden.widget.chat.copy_click_file"), (entry, button) -> {
                        String file = style.getClickEvent().getValue();
                        client.keyboard.setClipboard(file);
                        entry.setName(Text.translatable("reden.widget.chat.copied"));
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
