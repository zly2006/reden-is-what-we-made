package com.github.zly2006.reden.mixin.client.greenStone;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RedStoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class MixinRedstone {
    @Unique
    private final static int[] GREEN_COLORS = Util.make(new int[16], (is) -> {
        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float r = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float g = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float b = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            is[i] = 0xFF000000 | ((int)(r * 255.0F) << 16) | ((int)(g * 255.0F) << 8) | (int)(b * 255.0F);
        }
    });

    @Inject(
            method = "getColorForPower",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void overwriteColor(int powerLevel, CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().getResourcePackRepository().getSelectedIds().contains("reden:greenstone")) {
            cir.setReturnValue(GREEN_COLORS[powerLevel]);
        }
    }
}
