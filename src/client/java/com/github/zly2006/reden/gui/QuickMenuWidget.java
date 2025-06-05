package com.github.zly2006.reden.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class QuickMenuWidget implements NarratableEntry, Renderable, GuiEventListener {
    private final Screen parent;
    private final List<MenuEntry> entries = new ArrayList<>();
    private final Minecraft client = Minecraft.getInstance();
    int x;
    int y;
    int width;

    public QuickMenuWidget(Screen parent, int x, int y) {
        this.parent = parent;
        this.x = x;
        this.y = y;
    }

    public interface ClickAction {
        void onClick(MenuEntry entry, int button);
    }
    public static final ClickAction CLOSE_ACTION = (entry, button) -> entry.getParent().remove();
    public static final ClickAction EMPTY_ACTION = (entry, button) -> { };
    public class MenuEntry {
        Component name;
        ClickAction action;

        public MenuEntry(Component name, ClickAction action) {
            this.name = name;
            this.action = action;
        }

        QuickMenuWidget getParent() {
            return QuickMenuWidget.this;
        }

        public void setName(Component name) {
            this.name = name;
        }

        public void setAction(ClickAction action) {
            this.action = action;
        }

        public Component getName() {
            return name;
        }

        public ClickAction getAction() {
            return action;
        }
    }
    public void addEntry(Component name, ClickAction action) {
        entries.add(new MenuEntry(name, action));
    }

    public abstract void remove();

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (entries.isEmpty()) {
            remove();
            return;
        }
        int height = entries.size() * 14;
        width = Integer.max(entries.stream()
            .map(x -> client.font.width(x.name))
            .max(Integer::compareTo)
            .get(), 80);
        if (x + width > parent.width) {
            x = parent.width - width;
        }
        if (y + height > parent.height) {
            y = parent.height - height;
        }
        //? if <= 1.21.5 {
        /*context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, 100);
        context.fillGradient(RenderType.guiOverlay(), x, y, x + width, y + height, 0x80000000, 0x80000000, 0);
        *///?} else {
        context.pose().pushMatrix();
        context.pose().translate(0.0F, 0.0F, context.pose());
        context.fillGradient(x, y, x + width, y + height, 0x80000000, 0x80000000);
        //?}
        for (int i = 0; i < entries.size(); i++) {
            MenuEntry entry = entries.get(i);
            int color = 0xFFFFFF;
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + i * 14 && mouseY <= y + i * 14 + 14) {
                color = 0xFFFF00;
            }
            context.drawCenteredString(client.font, entry.name, x + width / 2, y + i * 14 + 2, color);
        }
        //? if <= 1.21.5
        /*context.pose().popPose();*/
        //? if >= 1.21.6
        context.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (entries.isEmpty()) {
            return false;
        }
        for (int i = 0; i < entries.size(); i++) {
            MenuEntry entry = entries.get(i);
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + i * 14 && mouseY <= y + i * 14 + 14) {
                ClickAction action = entry.action;
                entry.action = CLOSE_ACTION;
                action.onClick(entry, button);
                return true;
            }
        }
        remove();
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        remove();
        return false;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) { }
}
