package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.network.BreakPointInterrupt;
import com.github.zly2006.reden.network.GlobalStatus;
import com.github.zly2006.reden.transformers.RedenMixinExtension;
import com.github.zly2006.reden.transformers.UtilsKt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MixinServer extends ReentrantThreadExecutor<ServerTask> implements ServerData.ServerDataAccess {
    @Unique ServerData serverData = new ServerData(Reden.MOD_VERSION, (MinecraftServer) (Object) this);

    public MixinServer() {
        super("What?");
    }

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tick(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    private void tickStageTree(CallbackInfo ci) {
        serverData.realTicks++;
        // initialize the stage tree.
        assert serverData.getTickStage() != null;
        if (RedenMixinExtension.APPLY_DEBUGGER_MIXINS) {
            if (!serverData.getTickStageTree().getActiveStages().isEmpty()) {
                Reden.LOGGER.error("tree is not empty: {}", serverData.getTickStageTree().getActiveStages());
            }
            serverData.getTickStageTree().clear();
            UtilsKt.sendToAll((MinecraftServer) (Object) this, new BreakPointInterrupt(-1, null, false));
            serverData.getTickStageTree().push$reden_is_what_we_made(serverData.getTickStage());
        }
    }

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;endTickMetrics()V",
                    shift = At.Shift.AFTER
            )
    )
    private void tickStageTreeEnd(CallbackInfo ci) {
        if (RedenMixinExtension.APPLY_DEBUGGER_MIXINS) {
            serverData.getTickStageTree().pop$reden_is_what_we_made();
            if (serverData.getTickStageTree().getActiveStage() != null) {
                throw new IllegalStateException("Tick stage tree is not empty after popping all stages.");
            }
        }
    }

    @Inject(
            method = "stop",
            at = @At("HEAD")
    )
    private void stopping(CallbackInfo ci) {
        serverData.removeStatus(GlobalStatus.FROZEN);
        serverData.removeStatus(GlobalStatus.STARTED);
    }

    @NotNull
    @Override
    public ServerData getServerData$reden() {
        return serverData;
    }

    @Inject(
            method = "canExecute(Lnet/minecraft/server/ServerTask;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void patchAsync(ServerTask serverTask, CallbackInfoReturnable<Boolean> cir) {
        // Note: idk why
        if (serverData.isFrozen()) cir.setReturnValue(true);
    }
}
