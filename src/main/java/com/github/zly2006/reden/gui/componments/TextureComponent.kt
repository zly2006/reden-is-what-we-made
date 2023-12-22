package com.github.zly2006.reden.gui.componments

import com.github.zly2006.reden.mixin.otherMods.owo.ITextureComponent
import net.minecraft.util.Identifier

class TextureComponent(
    texture: Identifier, u: Int, v: Int, regionWidth: Int, regionHeight: Int, textureWidth: Int, textureHeight: Int
): io.wispforest.owo.ui.component.TextureComponent(
    texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight
) {
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun uv(u: Int, v: Int): TextureComponent {
        (this as ITextureComponent).setU(u)
        (this as ITextureComponent).setV(v)
        return this
    }
}
