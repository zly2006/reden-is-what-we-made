package com.github.zly2006.reden.mixin.clientGlow;

import com.github.zly2006.reden.clientGlow.ClientGlowKt;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
    @Shadow private World world;

    @Inject(
            method = "isGlowing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient && MalilibSettingsKt.ENABLE_CLIENT_GLOW.getBooleanValue() && !ClientGlowKt.getGlowing().isEmpty()) {
            cir.setReturnValue(ClientGlowKt.getGlowing().contains((Entity) (Object) this));
        }
    }
}
