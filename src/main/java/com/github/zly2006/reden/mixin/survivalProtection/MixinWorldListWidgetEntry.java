package com.github.zly2006.reden.mixin.survivalProtection;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class MixinWorldListWidgetEntry {
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
            BooleanConsumer bc = (bool) -> {
                if(bool) {
                    this.client.createIntegratedServerLoader().start(this.level.getName(), () -> {
                        ((IMixinWorldListWidget) this.field_19135).invokeLoadForReden();
                        this.client.setScreen(this.screen);
                    });
                } else {
                    this.client.setScreen(this.screen);
                }
            };

            Text title = Text.translatable("reden.widget.survival.title").formatted(Formatting.BOLD, Formatting.RED);
            Text warnings = Text.translatable("reden.widget.survival.warnings");
            this.client.setScreen(new ConfirmScreen(bc, title, warnings));
            ci.cancel();
        }
    }
}
