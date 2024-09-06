package com.github.zly2006.reden.minenv

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTextureComponent
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.text.Text
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MevDetailsScreen(val parent: Screen?, val info: MevItem) : BaseOwoScreen<FlowLayout>() {
    private val loadingLabel = Components.label(Text.literal("Loading image..."))!!
    private val images = ArrayList<Component>(info.images.size).apply {
        for (i in 0 until info.images.size) this.add(loadingLabel)
    }
    private val imgContainer = Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
        horizontalAlignment(HorizontalAlignment.CENTER)
    }!!
    private var imgId = 1
    private val imageInfoLabel = Components.label(Text.literal("Loading image..."))!!
    private val btnPrev = Components.button(Text.literal("<")) {
        imgId--
        if (imgId < 1) imgId = info.images.size
    }!!
    private val btnNext = Components.button(Text.literal(">")) {
        imgId++
        if (imgId > info.images.size) imgId = 1
    }!!

    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.child(Components.label(Text.literal(info.post_name)))
        rootComponent.child(
            Containers.verticalScroll(Sizing.fill(), Sizing.expand(),
                Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                    if (info.images.isNotEmpty()) {
                        this.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            child(btnPrev)
                            child(imageInfoLabel)
                            child(btnNext)
                            horizontalAlignment(HorizontalAlignment.CENTER)
                        })
                        info.images.mapIndexed { index, url ->
                            httpClient.newCall(Request.Builder().apply {
                                ua()
                                get()
                                url(url)
                            }.build()).apply {
                                Reden.LOGGER.info("Started request: ${request().url}")
                            }.enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {}

                                override fun onResponse(call: Call, response: Response) {
                                    val bytes = response.body!!.bytes()
                                    runCatching {
                                        WebTextureComponent(
                                            bytes,
                                            0,
                                            0,
                                            minOf(NativeImage.read(bytes).width, this@MevDetailsScreen.width)
                                        )
                                    }.onFailure {
                                        Reden.LOGGER.error("Image failed: $url")
                                    }.onSuccess {
                                        images[index] = it
                                    }
                                }
                            })
                        }
                        this.child(imgContainer)
                        this.child(Components.label(Text.of(info.description)).apply {
                            maxWidth(this@MevDetailsScreen.width)
                        })
                    }
                }
            ).apply {
                scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
            }
        )
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        imgContainer.child(0, images[imgId - 1])
        while (imgContainer.children().size > 1) {
            imgContainer.removeChild(imgContainer.children()[1])
        }
        imageInfoLabel.text(Text.literal("Image $imgId / ${info.images.size}"))
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.setScreen(parent)
    }
}
