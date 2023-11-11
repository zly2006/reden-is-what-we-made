package com.github.zly2006.reden.mixin.clientGlow;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(
            method = "tickEntity",
            at = @At("HEAD")
    )
    private void tickEntityHead(Entity entity, CallbackInfo ci) {
    }
}
