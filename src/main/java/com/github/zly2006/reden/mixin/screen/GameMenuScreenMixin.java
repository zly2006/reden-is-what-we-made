package com.github.zly2006.reden.mixin.screen;

import com.github.zly2006.reden.debugger.gui.ColorButton;
import com.github.zly2006.reden.debugger.gui.RedenPauseScreen;
import com.terraformersmc.modmenu.mixin.AccessorGridWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = {"initWidgets"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V"
            )},
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void modifyInit(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder, Text text) {
        if (gridWidget != null) {
            List<Widget> buttons = ((AccessorGridWidget)gridWidget).getChildren();
            ClickableWidget debuggerButton = new ColorButton(10, 10, 20, 20, Text.of("R"), 0xFF008FE1, (lambda) -> {
                this.client.setScreen(new RedenPauseScreen(Text.of("Reden Debugger")));
            });
            debuggerButton.setTooltip(Tooltip.of(Text.of("Reden Debugger Screen")));
            buttons.add(debuggerButton);
        }
    }
}
