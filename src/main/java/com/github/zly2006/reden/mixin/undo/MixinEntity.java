package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;id:I"
            )
    )
    private void beforeEntitySpawn(EntityType<?> type, World world, CallbackInfo ci) {
        if (!world.isClient) {
            UpdateMonitorHelper.isInitializingEntity = true;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onEntitySpawn(EntityType<?> type, World world, CallbackInfo ci) {
        if (!world.isClient) {
            UpdateMonitorHelper.entitySpawned((Entity) (Object) this);
        }
    }
}
