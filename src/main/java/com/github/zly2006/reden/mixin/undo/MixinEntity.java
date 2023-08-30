package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        UpdateMonitorHelper.tryAddRelatedEntity((Entity) (Object) this);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onEntitySpawn(EntityType type, World world, CallbackInfo ci) {
        if (!world.isClient) {
            UpdateMonitorHelper.entitySpawned((Entity) (Object) this);
        }
    }
}
