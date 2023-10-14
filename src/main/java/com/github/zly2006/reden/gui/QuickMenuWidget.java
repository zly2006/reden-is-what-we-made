package com.github.zly2006.reden.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class QuickMenuWidget implements Selectable, Drawable, Element {
    private final Screen parent;
    private final List<MenuEntry> entries = new ArrayList<>();
    private final MinecraftClient client = MinecraftClient.getInstance();
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
        Text name;
        ClickAction action;

        public MenuEntry(Text name, ClickAction action) {
            this.name = name;
            this.action = action;
        }

        QuickMenuWidget getParent() {
            return QuickMenuWidget.this;
        }

        public void setName(Text name) {
            this.name = name;
        }

        public void setAction(ClickAction action) {
            this.action = action;
        }

        public Text getName() {
            return name;
        }

        public ClickAction getAction() {
            return action;
        }
    }
    public void addEntry(Text name, ClickAction action) {
        entries.add(new MenuEntry(name, action));
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (entries.size() == 0) {
            return;
        }
        int height = entries.size() * 14;
        width = Integer.max(entries.stream()
            .map(x -> client.textRenderer.getWidth(x.name))
            .max(Integer::compareTo)
            .get(), 80);
        if (x + width > parent.width) {
            x = parent.width - width;
        }
        if (y + height > parent.height) {
            y = parent.height - height;
        }
        context.fillGradient(x, y, x + width, y + height, 0x80000000, 0x80000000);
        for (int i = 0; i < entries.size(); i++) {
            MenuEntry entry = entries.get(i);
            int color = 0xFFFFFF;
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + i * 14 && mouseY <= y + i * 14 + 14) {
                color = 0xFFFF00;
            }
            context.drawCenteredTextWithShadow(client.textRenderer, entry.name, x + width / 2, y + i * 14 + 2, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (entries.size() == 0) {
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

    public void remove() {
        parent.remove(this);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
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
    public SelectionType getType() {
        return SelectionType.NONE;
    }
    @Override
    public void appendNarrations(NarrationMessageBuilder builder) { }
}
