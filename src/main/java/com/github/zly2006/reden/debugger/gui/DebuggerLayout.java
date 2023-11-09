package com.github.zly2006.reden.debugger.gui;

import com.github.zly2006.reden.Reden;
import com.google.common.base.Strings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.Objects;

public class DebuggerLayout {
    private final int x;
    private final int y;
    private final TextRenderer textRenderer;
    private final MinecraftClient client;

    public DebuggerLayout(int x, int y, MinecraftClient client) {
        this.x = x;
        this.y = y;
        this.textRenderer = client.textRenderer;
        this.client = client;
    }

    public void render(DrawContext drawContext) {
        drawContext.draw(() -> {
            this.drawText(drawContext, this.getDebuggerText(), true);
        });
    }

    /**
     * This code is from [MinecraftClient#renderDebugInfoLeftText]
     */
    private void drawText(DrawContext context, List<String> text, boolean left) {
        Objects.requireNonNull(this.textRenderer);
        int i = 9;
        int j;
        String string;
        int k;
        int l;
        int m;
        for(j = 0; j < text.size(); ++j) {
            string = (String)text.get(j);
            if (!Strings.isNullOrEmpty(string)) {
                k = this.textRenderer.getWidth(string);
                l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
                m = 2 + i * j;
                context.fill(l - 1, m - 1, l + k + 1, m + i - 1, -1873784752);
            }
        }

        for(j = 0; j < text.size(); ++j) {
            string = (String)text.get(j);
            if (!Strings.isNullOrEmpty(string)) {
                k = this.textRenderer.getWidth(string);
                l = left ? 2 : context.getScaledWindowWidth() - 2 - k;
                m = 2 + i * j;
                context.drawText(this.textRenderer, string, l, m, 14737632, false);
            }
        }
    }

    private List<String> getDebuggerText() {
        return List.of(
                "Reden Debugger",
                "Version: " + Reden.MOD_VERSION,
                "**********[Update Stack]**********",
                "[+] NC from [0,0,0] to [0,0,0]",
                "[+] PP from [0,0,0] to [0,0,0]",
                "[+] Projectfile Hit [ArrorEntity]: UUID",
                "[+] TickEntity [ArrorEntity]: UUID",
                "[+] TickEntities / Overworld",
                "[+] WorldTick / Overworld",
                "[+] ServerTick: 20",
                "**********[Info]**********",
                "Type: Breakpoint fired",
                "Breakpoint: Block Breakpoint at BlockPos[1,0,0]",
                "Breakpoint Type: NC[to], PP[from->to]"
                );
    }
}
