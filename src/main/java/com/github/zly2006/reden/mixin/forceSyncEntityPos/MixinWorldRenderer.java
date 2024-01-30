package com.github.zly2006.reden.mixin.forceSyncEntityPos;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
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
