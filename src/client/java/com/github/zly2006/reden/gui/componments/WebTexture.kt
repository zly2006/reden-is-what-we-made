package com.github.zly2006.reden.gui.componments

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture

@Suppress("InconsistentCommentForJavaParameter")
class WebTexture(bytes: ByteArray) :
//? if <= 1.21.4
    /*DynamicTexture(NativeImage.read(bytes.inputStream())) {*/
//? if > 1.21.4
    DynamicTexture({"reden:web"}, NativeImage.read(bytes.inputStream())) {
    override fun getPixels(): NativeImage {
        return super.getPixels()!!
    }

    val image get() = pixels

    //? if < 1.21.4 {
    /*override fun upload() {
        this.bind()
        pixels.upload(
            /^ level = ^/ 0,
            /^ offsetX = ^/ 0,
            /^ offsetY = ^/ 0,
            /^ unpackSkipPixels = ^/ 0,
            /^ unpackSkipRows = ^/ 0,
            /^ width = ^/ pixels.width,
            /^ height = ^/ pixels.height,
            /^ blur = ^/ true,
            /^ clamp = ^/ false,
            /^ mipmap = ^/ true,
            /^ close = ^/ false
        )
    }
    *///?}
}
