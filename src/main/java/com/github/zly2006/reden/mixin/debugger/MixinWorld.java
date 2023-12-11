package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = World.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorld implements WorldAccess, AutoCloseable {
    @Shadow
    public abstract Profiler getProfiler();

    @Shadow
    private boolean iteratingTickingBlockEntities;

    @Shadow
    @Final
    private List<BlockEntityTickInvoker> pendingBlockEntityTickers;

    @Shadow
    @Final
    protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Shadow
    public abstract boolean shouldTickBlockPos(BlockPos pos);

}
