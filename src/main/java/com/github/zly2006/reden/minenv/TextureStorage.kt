package com.github.zly2006.reden.minenv

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTexture
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import net.minecraft.client.MinecraftClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object TextureStorage {
    private val cache = mutableMapOf<String, Result<WebTexture>>()
    fun getImage(url: String, action: (WebTexture) -> Unit) {
        MinecraftClient.getInstance().execute {
            if (url in cache && cache[url]!!.isSuccess) {
                action(cache[url]!!.getOrThrow())
                return@execute
            }

            httpClient.newCall(Request.Builder().apply {
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
                        if (response.header("content-type") != "image/png") {
                            cache[url] = Result.failure(Exception("Not PNG Files: $url"))
                            return
                        }
                        try {
                            val texture = WebTexture(response.body!!.bytes())
                            response.body!!.close()
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
