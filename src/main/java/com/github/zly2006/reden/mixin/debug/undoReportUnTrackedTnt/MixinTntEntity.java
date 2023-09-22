package com.github.zly2006.reden.mixin.debug.undoReportUnTrackedTnt;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class MixinTntEntity extends Entity implements UndoableAccess {
    public MixinTntEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            at = @At("RETURN")
    )
    private void onInit(EntityType<?> entityType, World world, CallbackInfo ci) {
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording == null) {
            if (MalilibSettingsKt.UNDO_REPORT_UN_TRACKED_TNT.getBooleanValue()) {
                Reden.LOGGER.error("TNT spawned, but no recording found");
            }
        }
    }
}
