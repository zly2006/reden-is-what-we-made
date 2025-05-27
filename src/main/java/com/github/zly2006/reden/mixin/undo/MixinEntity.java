package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity implements UndoableAccess {
    @Shadow private Level level;
    @Unique long undoId;

    @Override
    public long getUndoId$reden() {
        return undoId;
    }

    @Override
    public void setUndoId$reden(long undoId) {
        this.undoId = undoId;
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/Entity;id:I"
            )
    )
    private void beforeEntitySpawn(EntityType<?> entityType, Level level, CallbackInfo ci) {
        if (!level.isClientSide) {
            UpdateMonitorHelper.isInitializingEntity = true;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onEntitySpawn(EntityType<?> entityType, Level level, CallbackInfo ci) {
        if (!level.isClientSide) {
            UpdateMonitorHelper.entitySpawned((Entity) (Object) this);
        }
    }
}
