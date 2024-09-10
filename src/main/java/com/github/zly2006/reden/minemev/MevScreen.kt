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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MevScreen : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    var list: List<MevItem> = mutableListOf()
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
        set(value) {
            field = value
            btnPrev.active(page > 1)
            btnNext.active(page < totalPages)
            pageLabel.text(Text.literal("$page / $totalPages"))
        }
    val btnPrev = Components.button(Text.literal("<")) {
        page--
        it.active(false)
        doRequest()
    }
    val btnNext = Components.button(Text.literal(">")) {
        page++
        it.active(false)
        doRequest()
    }
    val pageLabel = Components.label(Text.empty())

    @Serializable
    class MevSearch(
        val posts: List<MevItem>,
        val total_pages: Int
    )

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
                val string = response.body.use {
                    it!!.string()
                }
                client!!.execute {
                    try {
                        val mevSearch = jsonIgnoreUnknown.decodeFromString<MevSearch>(string)
                        list = mevSearch.posts
                        totalPages = mevSearch.total_pages
                    } catch (e: Exception) {
                        list = jsonIgnoreUnknown.decodeFromString<List<MevItem>>(string)
                        totalPages = 100
                    }

                    listComponent.clearChildren()
                    list.map { mev ->
                        val component = Containers.horizontalFlow(Sizing.fixed(300), Sizing.fixed(40))
                        component.child(
                            Containers.verticalFlow(Sizing.expand(), Sizing.fixed(40)).apply {
                                this.child(Components.label(Text.literal(mev.post_name)))
                                this.child(Components.label(Text.literal("by ${mev.User}").formatted(GRAY)))
                                this.child(Components.label(Text.literal(mev.description)))
                            }
                        )
                        component.gap(5)
                        component.mouseDown().subscribe { _, _, b ->
                            if (b == 0) {
                                client!!.setScreen(MevDetailsScreen(this@MevScreen, mev))
                                true
                            } else false
                        }
                        mev.display = component
                        component.margins(Insets.vertical(3))
                    }.forEach { listComponent.child(it) }
                    if (list.isEmpty()) {
                        listComponent.child(
                            Components.label(Text.literal("Sorry, didn't found anything."))
                        )
                    }
                    list.forEach { mevItem ->
                        if (mevItem.images.isNotEmpty() && mevItem.display != null) {
                            val size = client!!.options.guiScale.value * 40 * 2
                            TextureStorage.getImage("https://www.minemev.com/api/preview/${mevItem.uuid}?size=$size") {
                                mevItem.display!!.child(0, WebTextureComponent(it, 0, 0, 40, 40))
                                if (DEBUG_MINENV_THUMBNAIL_COMPARISON.booleanValue) {
                                    TextureStorage.getImage(mevItem.images.first()) { rawImage ->
                                        mevItem.display!!.child(1, WebTextureComponent(rawImage, 0, 0, 40, 40))
                                    }
                                }
                            }
                        }
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
                },
                Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    child(btnPrev)
                    child(pageLabel)
                    child(btnNext)

                    verticalAlignment(VerticalAlignment.CENTER)
                }
            )
        )
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }
}
