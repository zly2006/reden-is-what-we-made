package com.github.zly2006.reden.debugger.gui;

import com.github.zly2006.reden.debugger.gui.hud.DebuggerHud;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Debug;

public class RedenPauseScreen extends Screen {
    private final GridLayout rDebuggerLayout;
    private final DebuggerLayout layout;
    private final MinecraftClient client;
    public RedenPauseScreen(Text title) {
        super(title);
        this.rDebuggerLayout = RDebuggerLayoutKt.RDebuggerLayout();
        this.client = MinecraftClient.getInstance();
        this.layout = new DebuggerLayout(10, 10, client);
    }

    @Override
    protected void init() {
        super.init();
        this.addOperationsButton();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        this.layout.render(drawContext);
//        rDebuggerLayout.draw(OwoUIDrawContext.of(drawContext), mouseX, mouseY, 1.0F, delta);

    }

    private void addOperationsButton() {
        final ClickableWidget stepInfoButton = new ColorButton(10, 150, 60, 20, Text.of("Step Info"), 0xFF008FE1, (lambda) -> {
            // TODO: add step info return
        });
        this.addDrawableChild(stepInfoButton);

        final ClickableWidget stepOverButton = new ColorButton(80, 150, 60, 20, Text.of("Step Over"), 0xFF008FE1, (lambda) -> {
            // TODO: add step over return
        });
        this.addDrawableChild(stepOverButton);

        final ClickableWidget stepTickButton = new ColorButton(150, 150, 60, 20, Text.of("Step Tick"), 0xFF008FE1, (lambda) -> {
            // TODO: add step tick return
        });
        this.addDrawableChild(stepTickButton);

        final ClickableWidget continueButton = new ColorButton(220, 150, 60, 20, Text.of("Continue"), 0xFF00FF00, (lambda) -> {
            // TODO: add continue return
        });
        this.addDrawableChild(continueButton);

        final ClickableWidget resetButton = new ColorButton(290, 150, 60, 20, Text.of("Reset"), 0xFFFF0000, (lambda) -> {
            // TODO: add reset return
        });
        this.addDrawableChild(resetButton);

        final ClickableWidget returnToHudButton = new ColorButton(360, 150, 60, 20, Text.of("HUD"), 0x00FFFFFF, (lambda) -> {
            this.client.setScreen(null);
            DebuggerHud.visible = !DebuggerHud.visible;
        });
        this.addDrawableChild(returnToHudButton);
    }
}
