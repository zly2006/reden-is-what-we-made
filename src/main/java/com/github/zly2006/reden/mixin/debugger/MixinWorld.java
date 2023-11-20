package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = World.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorld implements WorldAccess, AutoCloseable {
}
