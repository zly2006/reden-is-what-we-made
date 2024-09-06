package com.github.zly2006.reden.gui.componments

import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture

class WebTexture(bytes: ByteArray) : NativeImageBackedTexture(NativeImage.read(bytes.inputStream()))
