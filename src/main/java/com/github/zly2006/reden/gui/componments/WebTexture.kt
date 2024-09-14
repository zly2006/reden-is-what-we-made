package com.github.zly2006.reden.gui.componments

import com.github.zly2006.reden.Reden
import net.minecraft.client.resource.metadata.TextureResourceMetadata
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.texture.ResourceTexture
import net.minecraft.resource.ResourceManager

class WebTexture(bytes: ByteArray) : NativeImageBackedTexture(NativeImage.read(bytes.inputStream())) {
    override fun getImage(): NativeImage {
        return super.getImage()!!
    }

    override fun upload() {
        this.bindTexture()
        image.upload(
            /* level = */ 0,
            /* offsetX = */ 0,
            /* offsetY = */ 0,
            /* unpackSkipPixels = */ 0,
            /* unpackSkipRows = */ 0,
            /* width = */ image.width,
            /* height = */ image.height,
            /* blur = */ true,
            /* clamp = */ false,
            /* mipmap = */ true,
            /* close = */ false
        )
    }
}

class WebTexture1(bytes: ByteArray) : ResourceTexture(Reden.identifier("web_texture")) {
    val image = NativeImage.read(bytes.inputStream())!!
    override fun loadTextureData(resourceManager: ResourceManager?) = TextureData(
        TextureResourceMetadata(true, false),
        image
    )

    override fun close() {
        image.close()
        super.close()
    }
}
