package com.github.zly2006.reden.webmatic

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.componments.WebTextureComponent
import com.github.zly2006.reden.mixin.client.malilib.IMixinGuiListBase
import com.github.zly2006.reden.utils.multiver.Text
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

/**
 * Get the language code defined by reden.
 */
val Minecraft.lang: String get() = when (options.languageCode) {
    "en_us" -> "en"
    "zh_cn" -> "zh_cn"
    "zh_tw" -> "zh_tw"
    "ru_ru" -> "ru"
    else     -> "en"
}

class MevDetailsScreen(val parent: Screen?, val info: ItemDto) : BaseOwoScreen<FlowLayout>() {
    val client = Minecraft.getInstance()!!
    private val loadingLabel = Components.label(Text.literal("加载中...").withStyle(ChatFormatting.GRAY))!!
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
        if (false)
        httpClient.newCall(Request.Builder().apply {
            ua()
            get()
            url("https://minemev.com/api/details/${info.key}")
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
                }
            }
        })

        rootComponent.child(Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
            val text = Components.label(Text.literal(info.name)).apply {
                margins(Insets.vertical(7))
                horizontalSizing(Sizing.fill())
                horizontalTextAlignment(HorizontalAlignment.CENTER)
            }
            child(text)
            mouseEnter().subscribe {
                tooltip(Text.literal("在 RedenMC 网站 上查看详情").withStyle(ChatFormatting.YELLOW))
            }
            mouseLeave().subscribe {
                tooltip(null)
            }
            mouseDown().subscribe { _, _, b ->
                if (b == 0) {
                    Util.getPlatform().openUri("https://redenmc.com/${client.lang}/litematica/${info.key}")
                    true
                } else false
            }
        })
        rootComponent.child(
            Containers.verticalScroll(Sizing.fill(), Sizing.expand(),
                Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                    gap(5)
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
                    }
                    description.child(Components.label(Text.of(info.description)).apply {
                        sizing(Sizing.fill(), Sizing.content())
                    })
                    this.child(description)
                    this.child(Components.label(
                        Text.literal("点这里在 RedenMC 网站 上查看详情").withStyle(ChatFormatting.YELLOW)
                    ).apply {
                        mouseDown().subscribe { _, _, b ->
                            if (b == 0) {
                                Util.getPlatform().openUri("https://redenmc.com/${client.lang}/litematica/${info.key}")
                                true
                            } else false
                        }
                    })
                    this.child(Components.label(Text.of("\n文件下载")))
                    this.child(Components.label(Text.literal("敬请期待").withStyle(ChatFormatting.GRAY)))
                    this.child(filesContainer)
                    this.horizontalAlignment(HorizontalAlignment.CENTER)
                }
            ).apply {
                scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
            }
        )

        info.attachments.forEach { file ->
            filesContainer.child(FileComponent(file))
        }
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
    }

    private fun getUniqueFilename(file: FileItem, parent: Path): Path {
        //todo
        val extension = ".litematic"// + mapOf("world_download" to "zip").getOrDefault(file.name, file.name)
        val name = file.name.replace(extension, "")
        var path = parent.resolve(
            file.name + extension
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
        label.append(Text.literal(file.name).withStyle {
            it.withUnderlined(hover)
        })
        label.append(" ")
//        label.append(Text.literal("${file} Downloads").withStyle(ChatFormatting.GRAY))
        label.append("\n")
        label.append(Text.of(file.description).copy().withStyle(ChatFormatting.DARK_GREEN))
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
                        when (file.name.substringAfterLast('.')) {
                            "litematic"      -> openLitematica(path)
                            "world_download" -> openWorld(path, file)
                            else             -> error("Unknown file type: ${file.name}")
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

            val path = getUniqueFilename(file, Path("saves"))
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
                //? if < 1.21.6
                /*client.disconnect()*/
                //? if >= 1.21.6
                client.disconnectWithProgressScreen()
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
            /*schematicBrowser.switchToDirectory(path.parent.toFile())*/
            //? if >= 1.21.5
            schematicBrowser.switchToDirectory(path.parent)
            val entry = schematicBrowser.currentEntries.first {
                it.name == path.name
            }
            schematicBrowser.setLastSelectedEntry(
                entry, schematicBrowser.currentEntries.indexOf(entry)
            )
        }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        try {
            if (images.isNotEmpty()) {
                imgContainer.child(0, images[imgId - 1])
                while (imgContainer.children().size > 1) {
                    imgContainer.removeChild(imgContainer.children()[1])
                }
                imageInfoLabel.text(Text.literal("Image $imgId / ${info.images.size}"))
            }
        } catch (e: Exception) {
            Reden.LOGGER.error("Error rendering", e)
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClose() {
        client.setScreen(parent)
    }
}
