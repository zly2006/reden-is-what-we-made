package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.stages.ServerRootStage;
import com.github.zly2006.reden.network.GlobalStatus;
import com.github.zly2006.reden.transformers.RedenMixinExtension;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MixinServer extends ReentrantThreadExecutor<ServerTask> implements ServerData.ServerDataAccess {
    @Unique
    private static final String REDEN_BREAKPOINTS_JSON = "reden_breakpoints.json";
    @Shadow
    @Nullable
    private String serverId;
    @Shadow
    @Final
    protected LevelStorage.Session session;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        serverData = new ServerData(Reden.MOD_VERSION, (MinecraftServer) (Object) this);
    }

    @Unique
    ServerData serverData;

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
            MinecraftServer server = (MinecraftServer) (Object) this;
            if (!serverData.getTickStageTree().getActiveStages().isEmpty()) {
                Reden.LOGGER.error("tree is not empty: {}", serverData.getTickStageTree().getActiveStages());
            }
            serverData.getTickStageTree().clear();
            serverData.setTickStage(new ServerRootStage(server, serverData.realTicks));
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
            serverData.getTickStageTree().pop(ServerRootStage.class);
            serverData.setTickStage(null);
            if (serverData.getTickStageTree().getActiveStage() != null) {
                throw new IllegalStateException("Tick stage tree is not empty after popping all stages.");
            }
        }
    }

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void started(CallbackInfo ci) {
        serverData.addStatus(GlobalStatus.STARTED);
        serverData.getBreakpoints().load(session.getDirectory().path().resolve(REDEN_BREAKPOINTS_JSON));
    }

    @Inject(
            method = "stop",
            at = @At("HEAD")
    )
    private void stopping(CallbackInfo ci) {
        serverData.removeStatus(GlobalStatus.FROZEN);
        serverData.removeStatus(GlobalStatus.STARTED);
    }

    @Inject(
            method = "save",
            at = @At("HEAD")
    )
    private void saving(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        serverData.getBreakpoints().save(session.getDirectory().path().resolve(REDEN_BREAKPOINTS_JSON), suppressLogs);
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
    private void patchAsyncTasks(ServerTask serverTask, CallbackInfoReturnable<Boolean> cir) {
        // Note: idk why
        if (serverData.isFrozen()) cir.setReturnValue(true);
    }
}
