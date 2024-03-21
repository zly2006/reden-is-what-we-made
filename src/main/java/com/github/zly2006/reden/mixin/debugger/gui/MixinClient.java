package com.github.zly2006.reden.mixin.debugger.gui;

import com.github.zly2006.reden.access.ClientData;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.gui.DebuggerScreen;
import com.github.zly2006.reden.report.ReportKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinClient implements ServerData.ClientSideServerDataAccess, ClientData.ClientDataAccess {
    @Shadow @Nullable public Screen currentScreen;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow private @Nullable IntegratedServer server;

    @Shadow @Nullable public abstract ServerInfo getCurrentServerEntry();

    @Inject(
            method = "openGameMenu",
            at = @At("HEAD"),
            cancellable = true
    )
    private void redirectPauseMenu(boolean pauseOnly, CallbackInfo ci) {
        // if server is frozen, open the debugger
        ServerData data = getServerData$reden();
        if (data == null) {
            return;
        }
        if (currentScreen == null && data.isFrozen()) {
            setScreen(new DebuggerScreen(data.getTickStageTree(), getClientData$reden().getLastTriggeredBreakpoint()));
            ci.cancel();
        }
    }

    @Inject(
            method = "setWorld",
            at = @At("HEAD")
    )
    private void onDisconnect(ClientWorld world, CallbackInfo ci) {
        if (world == null) {
            ReportKt.onFunctionUsed("disconnect", false);
            new Thread(ReportKt::doHeartHeat).start();
        } else if (server != null) {
            ReportKt.onFunctionUsed("joinServer_local", false);
        } else {
            ReportKt.onFunctionUsed("joinServer_remote", false);
        }
    }
}
