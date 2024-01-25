package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.rvc.io.LitematicaIO
import com.github.zly2006.reden.rvc.io.SchematicStructure
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo.Companion.getWorldInfo
import com.github.zly2006.reden.utils.server
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.SmallCheckboxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer.Scrollbar
import io.wispforest.owo.ui.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtSizeTracker
import net.minecraft.network.NetworkSide
import net.minecraft.text.Text
import java.io.File
import java.io.FileFilter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.extension

class SelectionImportScreen(
    val fileType: Type = Type.Litematica,
) : BaseOwoScreen<FlowLayout>() {
    var selectedLine: FileLine? = null
    val importButton: ButtonComponent = Components.button(Text.literal("Import")) {
        onFunctionUsed("buttonImport_type${fileType.name}_importScreen")
        selectedLine?.let { fileType.import(it.file) }
    }.apply {
        active(selectedLine != null)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
            .padding(Insets.of(5, 0, 5, 0))

        rootComponent.gap(3)

        rootComponent.child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            child(Components.label(Text.literal("Import RVC Structure from:")))
            Type.values().forEach { type ->
                child(Components.button(type.displayName) {
                    client!!.setScreen(SelectionImportScreen(type))
                }.apply {
                    active(fileType != type)
                })
            }
        })
        rootComponent.child(Components.label(Text.literal("Please select a file to import")))
        rootComponent.child(
            Containers.verticalScroll(
                Sizing.fill(),
                Sizing.fill(75),
                Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                    fileType.discover(this@SelectionImportScreen, this)
                }).apply {
                scrollbar(Scrollbar.vanillaFlat())
            })
        rootComponent.child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            gap(5)
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            child(importButton)
        })
    }

    inner class FileLine(
        val file: File,
        val name: String,
    ) : FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        val left: FlowLayout = Containers.horizontalFlow(Sizing.fill(45), Sizing.content(1))
        val center: FlowLayout = Containers.horizontalFlow(Sizing.fill(10), Sizing.content(1))
        val right: FlowLayout = Containers.horizontalFlow(Sizing.fill(41), Sizing.content(1))
        val select: SmallCheckboxComponent = Components.smallCheckbox(Text.empty()).apply {
            onChanged().subscribe {
                if (it) {
                    selectedLine?.select?.checked(false)
                    selectedLine = this@FileLine
                } else {
                    selectedLine = null
                }
                importButton.active(selectedLine != null)
            }
            checked(selectedLine == this@FileLine)
        }

        init {
            gap(5)
            left.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            center.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
            right.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
            child(left)
            child(center)
            child(right)

            left.child(select)
            left.child(Components.label(Text.literal(name)))
            center.child(Components.label(Text.literal("${"%.2f".format((file.length() / 1024.0))}KB")))
            right.child(Components.label(Text.literal(file.lastModified().formatToDate())))
        }
    }

    fun Long.formatToDate(): String {
        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }

    companion object {
        const val FOLDER_SCHEMATICS = "schematics"
        const val EXTENSION_NBT = "nbt"
        const val EXTENSION_SCHEMATIC = "schematic"
        const val EXTENSION_LITEMATICA = "litematic"
        const val EXTENSION_RVC_ARCHIVE = "rvcarchive"
    }

    enum class Type(val displayName: Text) {
        StructureBlock(Text.literal("Structure Block")) {
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                server.session.directory.path.resolve("generated").toFile()
                    .listFiles(FileFilter { it.isDirectory })?.forEach {
                        val namespace = it.name
                        it.resolve("structures").listFiles()
                            ?.filter { it.extension == EXTENSION_NBT }
                            ?.forEach { structureFile ->
                                rootComponent.child(
                                    screen.FileLine(
                                        structureFile,
                                        if (namespace == "minecraft") structureFile.nameWithoutExtension
                                        else "$namespace:${structureFile.nameWithoutExtension}"
                                    )
                                )
                            }
                    }
            }
        },
        Litematica(Text.literal("Litematica")) {
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File(FOLDER_SCHEMATICS).mkdirs()
                File(FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                    .filter { !it.isDirectory && it.extension == EXTENSION_LITEMATICA }
                    .forEach { rootComponent.child(screen.FileLine(it, it.nameWithoutExtension)) }
            }

            override fun import(file: File): RvcRepository {
                val mc = MinecraftClient.getInstance()
                val repository = RvcRepository.create(file.nameWithoutExtension, mc.getWorldInfo(), NetworkSide.CLIENTBOUND)
                val structure = TrackedStructure(file.nameWithoutExtension, NetworkSide.CLIENTBOUND)
                LitematicaIO.load(file.toPath(), structure)
                repository.commit(structure, "Import from $file", mc.player)
                return repository
            }
        },
        RVCArchive(Text.literal("RVC Archive")){
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File(FOLDER_SCHEMATICS).mkdirs()
                File(FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                    .filter { !it.isDirectory && it.extension == EXTENSION_RVC_ARCHIVE }
                    .forEach { rootComponent.child(screen.FileLine(it, it.nameWithoutExtension)) }
            }

            private val json = Json {
                ignoreUnknownKeys
            }

            override fun import(file: File): RvcRepository {
                val zip = ZipFile(file)
                val mc = MinecraftClient.getInstance()
                val manifestString = zip.getInputStream(zip.getEntry("manifest.rvc.json")).readAllBytes().decodeToString()
                Reden.LOGGER.info("manifest: $manifestString")
                @Serializable
                class Manifest(
                    val name: String
                )
                val manifest = json.decodeFromString<Manifest>(manifestString)
                var name = manifest.name
                if (name in mc.data.rvcStructures) {
                    var i = 2
                    while (name in mc.data.rvcStructures) {
                        name = "${manifest.name} ($i)"
                        i++
                    }
                }
                val path = RvcRepository.path / name / ".git"
                for (entry in zip.entries()) {
                    val entryPath = path / entry.name
                    if (!entryPath.absolutePathString().contains("/.git/")) {
                        Reden.LOGGER.error("Invalid entry: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        entryPath.toFile().mkdirs()
                    } else {
                        entryPath.parent.toFile().mkdirs()
                        if (entryPath.parent.normalize() == path.normalize() && entryPath.extension == "json") {
                            Reden.LOGGER.info("Skipping $entryPath")
                            continue
                        }
                        entryPath.toFile().writeBytes(zip.getInputStream(entry).readAllBytes())
                        Reden.LOGGER.info("Extracted ${entry.name} to $entryPath")
                    }
                }
                val repository = RvcRepository.fromArchive(path, NetworkSide.CLIENTBOUND)
                return repository
            }
        },
        Other(Text.literal("Other")) {
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File(FOLDER_SCHEMATICS).mkdirs()
                File(FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                    .filterNot { it.isDirectory }
                    .filterNot { it.extension in setOf(EXTENSION_LITEMATICA, EXTENSION_RVC_ARCHIVE) } // ignore other formats
                 //   .filter { (it.extension in setOf("schematic", "schem")) }
                    .forEach { rootComponent.child(screen.FileLine(it, it.name)) }
            }
        };

        abstract fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout)

        /**
         * logic for structure template nbt files
         */
        open fun import(file: File): RvcRepository? {
            try {
                val mc = MinecraftClient.getInstance()
                val repository = RvcRepository.create(file.nameWithoutExtension, mc.getWorldInfo(), NetworkSide.CLIENTBOUND)
                val structure = TrackedStructure(file.nameWithoutExtension, NetworkSide.CLIENTBOUND)
                val nbt = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes())
                structure.assign(SchematicStructure().readFromNBT(nbt))
                repository.commit(structure, "Import from $file", mc.player)
                return repository
            } catch (e: Exception) {
                Reden.LOGGER.error("Failed to import structure from $file", e)
                return null
            }
        }
    }
}
