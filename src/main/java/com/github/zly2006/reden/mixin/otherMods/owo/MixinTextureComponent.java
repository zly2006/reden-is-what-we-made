package com.github.zly2006.reden.mixin.otherMods.owo;

import io.wispforest.owo.ui.component.TextureComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TextureComponent.class, remap = false)
public class MixinTextureComponent {
    @Mutable @Shadow @Final protected int u;

    @Mutable @Shadow @Final protected int v;
}
