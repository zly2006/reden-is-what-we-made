package com.github.zly2006.reden.mixin.forceSyncEntityPos;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    @Redirect(
            method = "renderEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    private <E extends Entity> void render(EntityRenderDispatcher dispatcher, E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (MalilibSettingsKt.TOGGLE_FORCE_ENTITY_POS_SYNC.getBooleanValue()) {
            dispatcher.render(entity, entity.getX() - cameraX, entity.getY() - cameraY, entity.getZ() - cameraZ, yaw, tickDelta, matrices, vertexConsumers, light);
        } else {
            dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }
}
