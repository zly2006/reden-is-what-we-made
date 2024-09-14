package com.github.zly2006.reden.minemev

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTextureComponent
import com.github.zly2006.reden.malilib.DEBUG_MINENV_THUMBNAIL_COMPARISON
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.jsonIgnoreUnknown
import com.github.zly2006.reden.report.ua
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting.GRAY
import net.minecraft.util.Formatting.UNDERLINE
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MevScreen : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    var list = mutableListOf<MevItem>()
    val listComponent = Containers.verticalFlow(Sizing.fill(), Sizing.content())!!.apply {
        horizontalAlignment(HorizontalAlignment.CENTER)
    }
    val search = Components.textBox(Sizing.fill())!!.apply {
        setPlaceholder(Text.literal("Search..."))
        onChanged().subscribe {
            page = 1
            doRequest()
        }
    }
    var page = 1
    var totalPages = 1

    @Serializable
    class MevSearch(
        val posts: List<MevItem>,
        val total_pages: Int
    )

    inner class PostComponent(val mev: MevItem, val isLast: Boolean) :
        FlowLayout(Sizing.fixed(300), Sizing.fixed(40), Algorithm.HORIZONTAL) {
        private val nameLabel = Components.label(Text.literal(mev.post_name))

        init {
            child(
                Containers.verticalFlow(Sizing.expand(), Sizing.fixed(40)).apply {
                    this.child(nameLabel)
                    this.child(Components.label(Text.literal("by ${mev.User}").formatted(GRAY)))
                    this.child(Components.label(Text.literal(mev.description)).apply {
                        lineSpacing(0)
                    })
                    gap(1)
                }
            )
            gap(5)
            margins(Insets.vertical(3))
            mouseDown().subscribe { _, _, b ->
                if (b == 0) {
                    client!!.setScreen(MevDetailsScreen(this@MevScreen, mev))
                    true
                } else false
            }
            mev.display = this

            if (mev.images.isNotEmpty()) {
                val size = client!!.options.guiScale.value * 40 * 2
                TextureStorage.getImage("https://www.minemev.com/api/preview/${mev.uuid}?size=$size") {
                    this.child(0, WebTextureComponent(it, 0, 0, 40, 40))
                    if (DEBUG_MINENV_THUMBNAIL_COMPARISON.booleanValue) {
                        TextureStorage.getImage(mev.images.first()) { rawImage ->
                            this.child(1, WebTextureComponent(rawImage, 0, 0, 40, 40))
                        }
                    }
                }
            }
        }

        val currentPage = page

        override fun draw(
            context: OwoUIDrawContext?,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
            delta: Float
        ) {
            if (this.isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())) {
                nameLabel.text(Text.literal(mev.post_name).formatted(UNDERLINE))
            } else {
                nameLabel.text(Text.literal(mev.post_name))
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (isLast && currentPage == page && page != totalPages) {
                page++
                doRequest()
            }
        }
    }

    private fun doRequest() {
        val requestStart = System.currentTimeMillis()
        httpClient.newCall(Request.Builder().apply {
            ua()
            get()
            url("https://minemev.com/api/search?search=${search.text}&page=$page")
        }.build()).apply {
            Reden.LOGGER.info("Started request: ${request().url}")
        }.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Reden.LOGGER.error("Failed request: ${call.request().url}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response.body!!.string()
                response.body!!.close()
                client!!.execute {
                    if (page == 1) {
                        listComponent.clearChildren()
                        list.clear()
                    }
                    val mevSearch = jsonIgnoreUnknown.decodeFromString<MevSearch>(string)
                    list.addAll(mevSearch.posts)
                    totalPages = mevSearch.total_pages

                    mevSearch.posts.forEachIndexed { index, mevItem ->
                        listComponent.child(PostComponent(mevItem, index == mevSearch.posts.size - 1))
                    }
                    if (list.isEmpty()) {
                        listComponent.child(
                            Components.label(Text.literal("Sorry, didn't found anything."))
                        )
                    }
                }
            }
        })
    }

    override fun build(rootComponent: FlowLayout) {
        listComponent.child(
            Components.label(Text.literal("Loading content..."))
        )
        doRequest()
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.children(
            listOf(
                search,
                Containers.verticalScroll(Sizing.fill(), Sizing.expand(), listComponent).apply {
                    scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                }
            )
        )
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }
}
