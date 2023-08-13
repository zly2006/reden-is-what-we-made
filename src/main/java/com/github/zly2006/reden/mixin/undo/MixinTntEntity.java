package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.ScheduledTickAccess;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(TntEntity.class)
public abstract class MixinTntEntity extends Entity implements ScheduledTickAccess {
    @Unique
    long undoId;

    public MixinTntEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            at = @At("RETURN")
    )
    private void onInit(EntityType<?> entityType, World world, CallbackInfo ci) {
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            if (MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue()) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("TNT spawned, adding it into record "+ recording.getId()));
            }
            undoId = recording.getId();
        }
    }

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        Explosion explosion = new Explosion(
                this.getWorld(),
                this,
                null,
                null,
                this.getX(),
                this.getBodyY(0.0625),
                this.getZ(),
                4.0f,
                false,
                this.getWorld().getDestructionType(GameRules.TNT_EXPLOSION_DROP_DECAY)
        );
        if (undoId != 0) {
            if (MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue()) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("TNT exploded, adding it into record "+ undoId));
            }
            ((ScheduledTickAccess) explosion).setUndoId(undoId);
        }
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);
        ci.cancel();
    }

    @Override
    public long getUndoId() {
        return undoId;
    }

    @Override
    public void setUndoId(long undoId) {
        this.undoId = undoId;
    }
}
