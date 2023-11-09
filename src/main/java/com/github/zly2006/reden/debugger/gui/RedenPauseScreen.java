package com.github.zly2006.reden.debugger.gui;

import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class RedenPauseScreen extends Screen {
    private final GridLayout rDebuggerLayout;
    public RedenPauseScreen(Text title) {
        super(title);
        this.rDebuggerLayout = RDebuggerLayoutKt.RDebuggerLayout();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        rDebuggerLayout.draw(OwoUIDrawContext.of(drawContext), mouseX, mouseY, 1.0F, delta);

    }

}
