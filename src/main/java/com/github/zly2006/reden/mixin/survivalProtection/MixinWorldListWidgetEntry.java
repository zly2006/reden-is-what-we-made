package com.github.zly2006.reden.mixin.survivalProtection;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.gui.message.ClientMessageQueue;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import kotlin.Unit;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class MixinWorldListWidgetEntry {
    @Unique
    private static final String NOTIFICATION_KEY = "reden:survival_save_warning";
    @Shadow
    private @Final MinecraftClient client;
    @Shadow
    private @Final LevelSummary level;
    @Shadow
    private @Final SelectWorldScreen screen;
    @Shadow(remap = false)
    private @Final WorldListWidget field_19135;

    @Inject(method = "play", at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/MinecraftClient"
                    + ".createIntegratedServerLoader()Lnet/minecraft/server/integrated/IntegratedServerLoader;"
    ),
            cancellable = true
    )
    private void requireConfirmIfNeeded(CallbackInfo ci) {
        if(this.level.getGameMode().isSurvivalLike() && !this.level.hasCheats()
                && MalilibSettingsKt.SURVIVAL_SAVE_PROTECTION.getBooleanValue()) {
            Text title = Text.translatable("reden.widget.survival.title").formatted(Formatting.BOLD, Formatting.RED);
            Text warnings = Text.translatable("reden.widget.survival.warnings");
            BooleanConsumer bc = (bool) -> {
                if(bool) {
                    sendWarningToast();
                    this.client.createIntegratedServerLoader().start(this.level.getName(), () -> {
                        ((IMixinWorldListWidget) this.field_19135).invokeLoadForReden();
                    });
                } else {
                    this.client.setScreen(this.screen);
                }
            };

            this.client.setScreen(new ConfirmScreen(bc, title, warnings));
            ci.cancel();
        }
    }

    @Unique
    private void sendWarningToast() {
        List<ClientMessageQueue.Button> buttons = new ArrayList<>();
        int id = ClientMessageQueue.INSTANCE.addNotification(NOTIFICATION_KEY,
                Reden.LOGO,
                Text.translatable("reden.widget.survival.title").formatted(Formatting.BOLD, Formatting.RED),
                Text.translatable("reden.widget.survival.risks"),
                buttons);
        buttons.add(new ClientMessageQueue.Button(
                Text.translatable("reden.widget.survival.more"), () -> openHelpPageAndDismissToast(id)));
        buttons.add(new ClientMessageQueue.Button(
                Text.translatable("reden.widget.survival.dismiss"),
                () -> {
                    ClientMessageQueue.INSTANCE.dontShowAgain(NOTIFICATION_KEY);
                    ClientMessageQueue.INSTANCE.remove(id);
                    return Unit.INSTANCE;
                }));
    }

    @Unique
    private Unit openHelpPageAndDismissToast(int id) {
        String url = switch(this.client.getLanguageManager().getLanguage()) {
            case "zh_cn" -> "https://wiki.redenmc.com/index.php/%E7%94%9F%E5%AD%98%E5%AD%98%E6%A1%A3%E4%BF%9D%E6%8A%A4";
            case "en_us" -> "https://wiki.redenmc.com/index.php/Survival_Save_Protection";
            default -> "https://wiki.redenmc.com/index.php/Survival_Save_Protection";
        };
        try {
            Util.getOperatingSystem().open(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException("This shouldn't happen!", e);
        }

        ClientMessageQueue.INSTANCE.remove(id);
        return Unit.INSTANCE;
    }
}
