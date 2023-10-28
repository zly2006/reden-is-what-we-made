package com.github.zly2006.reden.mixin.noTimeOut;

import com.github.zly2006.reden.RedenClient;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen extends Screen {
    @Shadow @Final private GridWidget grid;

    @Shadow @Final private Text reason;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void addSomething(CallbackInfo ci, GridWidget.Adder adder, ButtonWidget buttonWidget) {if (reason.getContent() instanceof TranslatableTextContent content && "disconnect.timeout".equals(content.getKey())) {
            if (MalilibSettingsKt.iSHOW_TIME_OUT_NOTIFICATION.getBooleanValue() && !MalilibSettingsKt.NO_TIME_OUT.getBooleanValue()) {
                adder.add(new TextWidget(
                        Text.of("If you are a developer debugging your server by breakpoints,\n try NoTimeOut provided by Reden Mod!"),
                        textRenderer
                ));
                adder.add(ButtonWidget.builder(
                        Text.literal("Enable NoTimeOut"),
                        s -> {
                            MalilibSettingsKt.NO_TIME_OUT.setBooleanValue(true);
                            RedenClient.saveMalilibOptions();
                        }
                ).build());
                adder.add(ButtonWidget.builder(
                        Text.literal("Dont show again"),
                        s -> {
                            MalilibSettingsKt.iSHOW_TIME_OUT_NOTIFICATION.setBooleanValue(false);
                            RedenClient.saveMalilibOptions();
                        }
                ).build());
            }
        }
    }
}
