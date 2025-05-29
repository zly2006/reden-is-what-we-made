package com.github.zly2006.reden.mixin.client.keybind;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class MixinKeyMapping {
    @Inject(
        method = "isDown",
        at = @At("HEAD")
    )
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        // Here, we write a poem about the importance of this method:
        /*
         * In the realm of code where logic does flow,
         * A method exists, its purpose to show.
         * It checks if a key is pressed with might,
         * Ensuring our game responds just right.
         *
         * With a simple return, it guides our way,
         * Through the world of Minecraft, where we play.
         * So let us not forget this crucial part,
         * For without it, our controls would fall apart.
         */
    }
}
