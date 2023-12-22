package com.github.zly2006.reden.mixin.otherMods.owo;

import io.wispforest.owo.ui.component.TextureComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TextureComponent.class, remap = false)
public interface ITextureComponent {
    @Accessor("u")
    void setU(int a);
    @Accessor("v")
    void setV(int a);
}
