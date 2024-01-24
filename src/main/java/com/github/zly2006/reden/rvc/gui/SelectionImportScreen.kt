package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.utils.server
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.SmallCheckboxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer.Scrollbar
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text
import java.io.File
import java.io.FileFilter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                selectedLine = if (it) {
                    selectedLine?.select?.checked(false)
                    this@FileLine
                } else {
                    null
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

            override fun import(file: File): Boolean {
                TODO()
            }
        },
        Litematica(Text.literal("Litematica")) {
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File(FOLDER_SCHEMATICS).mkdirs()
                File(FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                    .filter { !it.isDirectory && it.extension == EXTENSION_LITEMATICA }
                    .forEach { rootComponent.child(screen.FileLine(it, it.nameWithoutExtension)) }
            }

            override fun import(file: File): Boolean {
                TODO()
            }
        },
        RVCArchive(Text.literal("RVC Archive")){
            override fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File(FOLDER_SCHEMATICS).mkdirs()
                File(FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                    .filter { !it.isDirectory && it.extension == EXTENSION_RVC_ARCHIVE }
                    .forEach { rootComponent.child(screen.FileLine(it, it.nameWithoutExtension)) }
            }

            override fun import(file: File): Boolean {
                TODO()
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

            override fun import(file: File): Boolean {
                TODO()
            }
        };

        abstract fun discover(screen: SelectionImportScreen, rootComponent: FlowLayout)
        abstract fun import(file: File): Boolean
    }
}