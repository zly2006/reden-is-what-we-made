package com.github.zly2006.reden.mixin.debugger.gui;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.gui.DebuggerComponent;
import com.github.zly2006.reden.network.GlobalStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinClient implements ServerData.ClientSideServerDataAccess {
    @Shadow @Nullable public Screen currentScreen;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Inject(
            method = "openPauseMenu",
            at = @At("HEAD"),
            cancellable = true
    )
    private void redirectPauseMenu(boolean pause, CallbackInfo ci) {
        // if server is frozen, open the debugger
        ServerData data = getServerData$reden();
        if (data == null) {
            return;
        }
        if (currentScreen == null && data.hasStatus(GlobalStatus.FROZEN)) {
            setScreen(new DebuggerComponent(data.getTickStageTree()).asScreen());
            ci.cancel();
        }
    }
}
