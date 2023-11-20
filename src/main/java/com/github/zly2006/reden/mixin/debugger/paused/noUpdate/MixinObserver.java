package com.github.zly2006.reden.mixin.debugger.paused.noUpdate;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.network.GlobalStatus;
import net.minecraft.block.ObserverBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(value = ObserverBlock.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinObserver {
    @Inject(
            method = "scheduleTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelObserverPP(WorldAccess world, BlockPos pos, CallbackInfo ci) {
        if (world instanceof ServerWorld sw) {
            boolean frozen = data(sw.getServer()).hasStatus(GlobalStatus.FROZEN);
            if (frozen) {
                ci.cancel();
            }
        }
    }
}
