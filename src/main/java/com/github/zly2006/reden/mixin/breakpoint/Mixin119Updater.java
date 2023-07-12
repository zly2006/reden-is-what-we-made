package com.github.zly2006.reden.mixin.breakpoint;

import com.github.zly2006.reden.access.ChainedUpdaterView;
import com.github.zly2006.reden.mixinhelper.BreakpointHelper;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class Mixin119Updater implements ChainedUpdaterView {
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull BreakpointHelper helper;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(World world, int maxChainDepth, CallbackInfo ci) {
        helper = new BreakpointHelper(world);
    }

    @NotNull
    @Override
    public BreakpointHelper getBreakpointHelper() {
        return helper;
    }
}
