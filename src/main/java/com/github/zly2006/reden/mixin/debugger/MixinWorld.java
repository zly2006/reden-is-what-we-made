package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.debugger.stages.world.BlockEntitiesRootStage;
import com.github.zly2006.reden.debugger.stages.world.BlockEntityStage;
import com.github.zly2006.reden.debugger.tree.TickStageTree;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.github.zly2006.reden.access.ServerData.getData;

@Mixin(value = World.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorld implements WorldAccess, AutoCloseable {
    @Shadow
    @Final
    private List<BlockEntityTickInvoker> pendingBlockEntityTickers;

    @Shadow
    @Final
    protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Shadow @Final public boolean isClient;

    @Inject(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeBlockEntityTick(CallbackInfo ci, @Local BlockEntityTickInvoker blockEntityTickInvoker) {
        if (isClient) return;
        TickStageTree tree = getData(getServer()).getTickStageTree();
        tree.push$reden_is_what_we_made(new BlockEntityStage(
                (BlockEntitiesRootStage) tree.getActiveStage(),
                blockEntityTickInvoker
        ));
    }

    @Inject(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterBlockEntityTick(CallbackInfo ci) {
        if (isClient) return;
        Assertions.assertInstanceOf(BlockEntityStage.class, getData(getServer()).getTickStageTree().pop$reden_is_what_we_made());
    }
}
