package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.render.BlockBorder;
import com.github.zly2006.reden.render.BlockOutline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    @Nullable
    public ClientWorld world;

    @Inject(
            method = "setWorld",
            at = @At("HEAD")
    )
    private void onWorldChange(ClientWorld world, CallbackInfo ci) {
        // clear all renderers
        BlockBorder.INSTANCE.getTags$reden_is_what_we_made().clear();
        BlockOutline.INSTANCE.getBlocks().clear();
    }
}
