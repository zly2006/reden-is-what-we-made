package com.github.zly2006.reden.mixin.greenStone;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class MixinRedstone {
    @Inject(
            method = "getWireColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;packRgb(FFF)I"
            ),
            cancellable = true
    )
    private static void overwriteColor(int powerLevel, CallbackInfoReturnable<Integer> cir, @Local Vec3d vec3d) {
        if (MinecraftClient.getInstance().getResourcePackManager().getEnabledIds().contains("reden:greenstone")) {
            // swap red and green channel
            cir.setReturnValue(MathHelper.packRgb((float) vec3d.getY(), (float) vec3d.getX(), (float) vec3d.getZ()));
        }
    }
}
