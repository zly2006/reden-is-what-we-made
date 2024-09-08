package com.github.zly2006.reden.minenv

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTextureComponent
import com.github.zly2006.reden.mixin.malilib.IMixinGuiListBase
import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.jsonIgnoreUnknown
import com.github.zly2006.reden.report.ua
import com.github.zly2006.reden.utils.red
import fi.dy.masa.litematica.gui.GuiSchematicLoad
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
private class FileItem(
    val default_file_name: String,
    @SerialName("file")
    val url: String,
    val file_size: Int,
    val versions: List<String>,
    val downloads: Int,
    val file_type: String
)

class MevDetailsScreen(val parent: Screen?, val info: MevItem) : BaseOwoScreen<FlowLayout>() {
    private val loadingLabel = Components.label(Text.literal("Loading image...").formatted(Formatting.GRAY))!!
    private val images = ArrayList<Component>(info.images.size).apply {
        for (i in 0 until info.images.size) this.add(loadingLabel)
    }
    private val imgContainer = Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
        horizontalAlignment(HorizontalAlignment.CENTER)
    }!!
    private val filesContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
    }!!
    private var imgId = 1
    private val imageInfoLabel = Components.label(Text.empty().formatted(Formatting.GRAY))!!
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
        rootComponent.child(Components.label(Text.literal(info.post_name).styled {
            it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.minemev.com/p/${info.uuid}"))
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("View on minemev.com")))
        }).apply {
            margins(Insets.vertical(7))
            horizontalSizing(Sizing.fill())
            horizontalTextAlignment(HorizontalAlignment.CENTER)
        })
        rootComponent.child(
            Containers.verticalScroll(Sizing.fill(), Sizing.expand(),
                Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                    if (info.images.isNotEmpty()) {
                        this.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            child(btnPrev)
                            child(imageInfoLabel)
                            child(btnNext)
                            horizontalAlignment(HorizontalAlignment.CENTER)
                            verticalAlignment(VerticalAlignment.CENTER)
                        })
                        info.images.mapIndexed { index, url ->
                            TextureStorage.getImage(url, {
                                images[index] = WebTextureComponent.fixedHeight(
                                    it, 0, 0,
                                    this@MevDetailsScreen.height * 4 / 5
                                )
                            }) {
                                images[index] = Components.label(Text.literal("Failed: ${it.message}").red())
                            }
                        }
                        this.child(imgContainer)
                        this.child(Components.label(Text.of(info.description)).apply {
                            sizing(Sizing.fill(), Sizing.content())
                        })
                        this.child(Components.label(Text.of("\n\nFile Downloads")))
                        this.child(filesContainer)
                        this.horizontalAlignment(HorizontalAlignment.CENTER)
                    }
                }
            ).apply {
                scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
            }
        )

        httpClient.newCall(Request.Builder().apply {
            ua()
            get()
            url("https://www.minemev.com/api/files/${info.uuid}")
        }.build()).apply {
            Reden.LOGGER.info("Started request: ${request().url}")
        }.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val fileItems = jsonIgnoreUnknown.decodeFromString<List<FileItem>>(response.body!!.use { it.string() })
                client!!.execute {
                    fileItems.forEach { file ->
                        val label = Text.literal(file.default_file_name)
                        label.append(" ")
                        label.append(Text.literal("${file.downloads} Downloads").formatted(Formatting.GRAY))
                        label.append("\n")
                        label.append(Text.literal(file.versions.joinToString(" ")).formatted(Formatting.DARK_GREEN))
                        filesContainer.child(Components.label(label).apply {
                            mouseDown().subscribe { _, _, b ->
                                if (b == 0) {
                                    val parent = Path("schematics", "downloaded")
                                    parent.createDirectories()
                                    val path = getUniqueFilename(file, parent)
                                    httpClient.newCall(Request.Builder().apply {
                                        ua()
                                        get()
                                        url(file.url)
                                    }.build()).apply {
                                        Reden.LOGGER.info("Started request: ${request().url}")
                                    }.execute().body!!.use {
                                        path.writeBytes(it.bytes())
                                    }
                                    runCatching {
                                        val guiSchematicLoad = GuiSchematicLoad()
                                        guiSchematicLoad.parent = this@MevDetailsScreen
                                        client!!.setScreen(guiSchematicLoad)
                                        @Suppress("UNCHECKED_CAST")
                                        val schematicBrowser =
                                            (guiSchematicLoad as IMixinGuiListBase<DirectoryEntry,
                                                    WidgetDirectoryEntry, WidgetSchematicBrowser>).`widget$reden`()
                                        schematicBrowser.switchToDirectory(parent.toFile())
                                        val entry = schematicBrowser.currentEntries.first {
                                            it.name == path.name
                                        }
                                        schematicBrowser.setLastSelectedEntry(
                                            entry, schematicBrowser.currentEntries.indexOf(entry)
                                        )
                                    }.onFailure {
                                        Reden.LOGGER.error("Error opening $path", it)
                                        Util.getOperatingSystem().open(file.url)
                                    }
                                    true
                                } else false
                            }
                        })
                    }
                }
            }

            private fun getUniqueFilename(file: FileItem, parent: Path): Path {
                val addExtension = !file.default_file_name.endsWith(".${file.file_type}")
                var path = parent.resolve(
                    file.default_file_name + if (addExtension) ".${file.file_type}" else ""
                )
                if (path.exists()) {
                    var i = 2
                    while (path.exists()) {
                        path = parent.resolve(
                            file.default_file_name.substringBeforeLast('.') +
                                    " ($i)" + file.default_file_name.substringAfterLast('.') +
                                    if (addExtension) ".${file.file_type}" else ""
                        )
                        i++
                    }
                }
                return path
            }
        })
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
