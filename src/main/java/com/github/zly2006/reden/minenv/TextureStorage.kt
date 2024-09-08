package com.github.zly2006.reden.minenv

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTexture
import com.github.zly2006.reden.malilib.DEBUG_MINENV_NO_CACHE
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import net.minecraft.client.MinecraftClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

object TextureStorage {
    private val cache = mutableMapOf<String, Result<WebTexture>>()

    fun getImage(url: String, action: (WebTexture) -> Unit) {
        getImage(url, action) {}
    }

    fun getImage(url: String, action: (WebTexture) -> Unit, failed: (Throwable) -> Unit) {
        MinecraftClient.getInstance().execute {
            if (url in cache && !DEBUG_MINENV_NO_CACHE.booleanValue) {
                cache[url]!!.onSuccess(action).onFailure(failed)
                return@execute
            }

            httpClient.newCall(Request.Builder().apply {
                Reden.LOGGER.info("Started getting image: $url")
                ua()
                get()
                url(url)
            }.build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Reden.LOGGER.error("Error request: $url", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) return
                        var bytes = response.body!!.bytes()
                        try {
                            if (response.header("content-type") != "image/png") {
                                val image = ImageIO.read(bytes.inputStream())
                                if (image == null) {
                                    cache[url] =
                                        Result.failure(Exception("unknown image format: " + response.header("content-type")))
                                }
                                ByteArrayOutputStream().use {
                                    ImageIO.write(image, "png", it)
                                    bytes = it.toByteArray()
                                }
                            }
                            val texture = WebTexture(bytes)
                            MinecraftClient.getInstance().execute {
                                cache[url] = Result.success(texture)
                                action(texture)
                            }
                        } catch (e: Throwable) {
                            Reden.LOGGER.error("Error reading image: $url", e)
                        }
                    }
                }
            })
        }
    }
}
