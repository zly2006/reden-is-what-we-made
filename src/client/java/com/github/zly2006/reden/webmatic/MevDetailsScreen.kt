package com.github.zly2006.reden.webmatic

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTextureComponent
import com.github.zly2006.reden.minemev.MevItem
import com.github.zly2006.reden.mixin.client.malilib.IMixinGuiListBase
import com.github.zly2006.reden.utils.multiver.Text
import com.github.zly2006.reden.utils.multiver.clickOpenUrl
import com.github.zly2006.reden.utils.multiver.hoverShowText
import com.github.zly2006.reden.utils.red
import fi.dy.masa.litematica.gui.GuiSchematicLoad
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.network.chat.MutableComponent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.use
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.*

@Serializable
data class FileItem(
    val default_file_name: String,
    @SerialName("file")
    val url: String,
    val file_size: Int,
    val versions: List<String>,
    val downloads: Int,
    val file_type: String
)

class MevDetailsScreen(val parent: Screen?, val info: MevItem) : BaseOwoScreen<FlowLayout>() {
    val client = Minecraft.getInstance()!!
    private val loadingLabel = Components.label(Text.literal("Loading image...").withStyle(ChatFormatting.GRAY))!!
    private val images = ArrayList<Component>(info.images.size).apply {
        for (i in 0 until info.images.size) this.add(loadingLabel)
    }
    private val imgContainer = Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
        horizontalAlignment(HorizontalAlignment.CENTER)
    }!!
    private val filesContainer = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
    }!!
    private val description = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
    }!!
    private var imgId = 1
    private val imageInfoLabel = Components.label(Text.empty().withStyle(ChatFormatting.GRAY))!!
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
        httpClient.newCall(Request.Builder().apply {
            ua()
            get()
            url("https://minemev.com/api/details/${info.uuid}")
        }.build()).apply {
            Reden.LOGGER.info("Started request: ${request().url}")
        }.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e.message != "Canceled") {
                    Reden.LOGGER.error("Failed request: ${call.request().url}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body!!.use {
                    val string = it.string()
                    val item = jsonIgnoreUnknown.decodeFromString<MevItem>(string)
                    client!!.execute {
                        description.child(Components.label(Text.of(item.description)).apply {
                            sizing(Sizing.fill(), Sizing.content())
                        })
                        while (description.children().size > 1) {
                            description.removeChild(description.children().first())
                        }
                    }
                }
            }
        })

        rootComponent.child(Components.label(Text.literal(info.post_name)
            .clickOpenUrl("https://www.minemev.com/p/${info.uuid}")
            .hoverShowText("View on minemev.com")
        ).apply {
            margins(Insets.vertical(7))
            horizontalSizing(Sizing.fill())
            horizontalTextAlignment(HorizontalAlignment.CENTER)
        })
        rootComponent.child(
            Containers.verticalScroll(Sizing.fill(), Sizing.expand(),
                Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                    if (info.images.isNotEmpty()) {
                        this.child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            child(btnPrev as Component)
                            child(imageInfoLabel)
                            child(btnNext as Component)
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
                                images[index] = Components.label(Text.literal("Failed: ${it.message}").red()).apply {
                                    maxWidth(this@MevDetailsScreen.width)
                                }
                            }
                        }
                        this.child(imgContainer)
                        description.child(Components.label(Text.of(info.description)).apply {
                            sizing(Sizing.fill(), Sizing.content())
                        })
                        this.child(description)
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
                        filesContainer.child(FileComponent(file))
                    }
                }
            }
        })
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
    }

    private fun getUniqueFilename(file: FileItem, parent: Path): Path {
        val extension = "." + mapOf("world_download" to "zip").getOrDefault(file.file_type, file.file_type)
        val name = file.default_file_name.replace(extension, "")
        var path = parent.resolve(
            file.default_file_name + extension
        )
        if (path.exists()) {
            var i = 2
            while (path.exists()) {
                path = parent.resolve(
                    "$name ($i)$extension"
                )
                i++
            }
        }
        return path
    }

    private fun getLabel(file: FileItem, hover: Boolean): MutableComponent {
        val label = Text.empty()
        label.append(Text.literal(file.default_file_name).withStyle {
            it.withUnderlined(hover)
        })
        label.append(" ")
        label.append(Text.literal("${file.downloads} Downloads").withStyle(ChatFormatting.GRAY))
        label.append("\n")
        label.append(Text.literal(file.versions.joinToString(" ")).withStyle(ChatFormatting.DARK_GREEN))
        return label
    }

    inner class FileComponent(
        private val file: FileItem
    ) : LabelComponent(getLabel(file, false)) {
        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            this.text(getLabel(file, isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())))
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }

        init {
            mouseDown().subscribe { _, _, b ->
                if (b == 0) {
                    val parent = Path("schematics", "reden-downloads")
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
                        when (file.file_type) {
                            "litematic"      -> openLitematica(path)
                            "world_download" -> openWorld(path, file)
                            else             -> error("Unknown file type: ${file.file_type}")
                        }
                    }.onFailure {
                        Reden.LOGGER.error("Error opening $path", it)
                        Util.getPlatform().openUri(file.url)
                    }
                    true
                } else false
            }
        }

        private fun openWorld(zipPath: Path, file: FileItem) {
            val levelDat = ZipFile(zipPath.toFile()).entries().iterator().asSequence()
                .map { it.name }
                .filter { it.endsWith("level.dat") }.sortedBy { it.length }.firstOrNull()
                ?: error("Bad zip file: not a save")
            val prefix = levelDat.removeSuffix("level.dat")

            val path = getUniqueFilename(file.copy(file_type = "unzipped"), Path("saves"))
            ZipInputStream(zipPath.toFile().inputStream().buffered()).use { stream ->
                while (true) {
                    val entry = stream.nextEntry ?: break
                    if (!entry.isDirectory) {
                        path.resolve(entry.name.removePrefix(prefix))
                            .createParentDirectories()
                            .outputStream().buffered()
                            .use { out -> stream.copyTo(out) }
                    }
                }
            }
            if (client.connection != null) {
                client.disconnect()
            }
            val select = SelectWorldScreen(this@MevDetailsScreen)
            client.setScreen(select)
//            select.list.pendingLevels.join()
//            select.list.show(select.list.levelsFuture.getNow(null))
//            val entry = select.list.children().firstOrNull {
//                it is WorldSelectionList.WorldListEntry && it.summary.name == path.name
//            }
//            select.list.setSelected(entry)
//            if (entry != null) {
//                val index = select.list.children().indexOf(entry)
//                select.list.scrollAmount = select.list.getRowTop(index).toDouble() - 52
//            }
        }

        private fun openLitematica(path: Path) {
            val guiSchematicLoad = GuiSchematicLoad()
            guiSchematicLoad.parent = this@MevDetailsScreen
            client!!.setScreen(guiSchematicLoad)
            @Suppress("UNCHECKED_CAST")
            val schematicBrowser =
                (guiSchematicLoad as IMixinGuiListBase<DirectoryEntry,
                        WidgetDirectoryEntry, WidgetSchematicBrowser>).`widget$reden`()
            //? if < 1.21.5
            schematicBrowser.switchToDirectory(path.parent.toFile())
            //? if >= 1.21.5
            /*schematicBrowser.switchToDirectory(path.parent)*/
            val entry = schematicBrowser.currentEntries.first {
                it.name == path.name
            }
            schematicBrowser.setLastSelectedEntry(
                entry, schematicBrowser.currentEntries.indexOf(entry)
            )
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        imgContainer.child(0, images[imgId - 1])
        while (imgContainer.children().size > 1) {
            imgContainer.removeChild(imgContainer.children()[1])
        }
        imageInfoLabel.text(Text.literal("Image $imgId / ${info.images.size}"))
        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClose() {
        client.setScreen(parent)
    }
}
