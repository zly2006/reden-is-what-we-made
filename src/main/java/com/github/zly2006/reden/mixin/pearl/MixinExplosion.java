package com.github.zly2006.reden.mixin.pearl;

import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Explosion.class, priority = 100)
public class MixinExplosion {
}
