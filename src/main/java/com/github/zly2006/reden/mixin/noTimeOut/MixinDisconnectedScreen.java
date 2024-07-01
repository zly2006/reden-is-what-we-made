package com.github.zly2006.reden.mixin.noTimeOut;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.RedenClientKt;
import com.github.zly2006.reden.gui.message.ClientMessageQueue;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import kotlin.Unit;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen extends Screen {
    @Shadow
    @Final
    private DirectionalLayoutWidget grid;

    @Shadow
    @Final
    private DisconnectionInfo info;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void tryNoTimeOut(CallbackInfo ci, ButtonWidget buttonWidget) {
        if (this.info.reason().getContent() instanceof TranslatableTextContent content && "disconnect.timeout".equals(content.getKey())) {
            if (!MalilibSettingsKt.NO_TIME_OUT.getBooleanValue()) {
                var buttonList = new ArrayList<ClientMessageQueue.Button>();
                int id = ClientMessageQueue.INSTANCE.addNotification(
                        "reden:no_time_out",
                        Reden.LOGO,
                        Text.of("Enable NoTimeOut"),
                        Text.of("If you are a developer debugging your server by breakpoints,\ntry NoTimeOut provided by Reden Mod! It will prevent you from being kicked out of the server!"),
                        buttonList
                );
                buttonList.add(new ClientMessageQueue.Button(
                        Text.of("Enable NoTimeOut"),
                        () -> {
                            MalilibSettingsKt.NO_TIME_OUT.setBooleanValue(true);
                            RedenClientKt.saveMalilibOptions();
                            ClientMessageQueue.INSTANCE.remove(id);
                            return Unit.INSTANCE;
                        }
                ));
                buttonList.add(new ClientMessageQueue.Button(
                        Text.of("Dont show again"),
                        () -> {
                            ClientMessageQueue.INSTANCE.remove(id);
                            ClientMessageQueue.INSTANCE.dontShowAgain("reden:no_time_out");
                            return Unit.INSTANCE;
                        }
                ));
            }
        }
    }
}
