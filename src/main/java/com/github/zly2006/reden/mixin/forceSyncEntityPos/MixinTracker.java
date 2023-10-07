package com.github.zly2006.reden.mixin.forceSyncEntityPos;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class MixinTracker {
    @Shadow @Final private Entity entity;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"
            )
    )
    private void onSendPacket(Consumer instance, Object t) {
        if (t instanceof EntityS2CPacket && MalilibSettingsKt.TOGGLE_FORCE_ENTITY_POS_SYNC.getBooleanValue()) {
            instance.accept(new EntityPositionS2CPacket(this.entity));
        }
        instance.accept(t);
    }
}
