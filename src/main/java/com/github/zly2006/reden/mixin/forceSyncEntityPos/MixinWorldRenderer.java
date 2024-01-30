package com.github.zly2006.reden.mixin.forceSyncEntityPos;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    private double cameraX;
    private double cameraY;
    private double cameraZ;

    @Inject(
            method = "renderEntity",
            at = @At("HEAD")
    )
    private void captureArgs(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
    }

    @ModifyVariable(
            method = "renderEntity",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyDelta(float tickDelta) {
        if (MalilibSettingsKt.TOGGLE_FORCE_ENTITY_POS_SYNC.getBooleanValue()) {
            return 1f; // the real position
        }
        return tickDelta;
    }
}
