package com.github.zly2006.reden.debugger.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

// https://github.com/BotW-Minecraft-Server/RealmsHost/blob/main/src/main/java/link/botwmcs/samchai/realmshost/client/gui/components/ColorButton.java
@Environment(EnvType.CLIENT)
public class ColorButton extends ButtonWidget {
    int color;
    public static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    public ColorButton(int x, int y, int buttonWidth, int buttonHeight, Text component, int color, PressAction onPress) {
        super(x, y, buttonWidth, buttonHeight, component, onPress, Supplier::get);
        this.color = color;
    }

    @Override
    protected void renderButton(DrawContext drawContext, int mouseX, int mouseY, float partialTick) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (this.active) {
            drawContext.setShaderColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f, this.alpha);
        } else {
            drawContext.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        }
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        drawContext.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 16777215 : 10526880;
        this.drawMessage(drawContext, minecraft.textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isSelected()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}
