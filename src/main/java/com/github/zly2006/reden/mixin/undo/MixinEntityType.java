package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Deprecated
@Mixin(EntityType.class)
public class MixinEntityType {
    @Inject(
            method = "create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;",
            at = @At("HEAD")
    )
    private void beforeEntitySpawn(World world, CallbackInfoReturnable<?> cir) {
        if (!world.isClient) {
            UpdateMonitorHelper.isInitializingEntity = true;
        }
    }

    @Inject(
            method = "create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;",
            at = @At("RETURN")
    )
    private void afterEntitySpawn(World world, CallbackInfoReturnable<? extends Entity> cir) {
        if (!world.isClient) {
            Entity entity = cir.getReturnValue();
            if (entity != null) {
                UpdateMonitorHelper.entitySpawned(entity);
            }
            UpdateMonitorHelper.isInitializingEntity = false;
        }
    }
}
