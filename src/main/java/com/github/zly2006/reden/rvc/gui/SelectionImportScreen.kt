package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.report.onFunctionUsed
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
import java.io.IOException
import java.nio.file.*
import java.nio.file.Files.walkFileTree
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SelectionImportScreen(
    val fileType: Type = Type.StructureBlock,
) : BaseOwoScreen<FlowLayout>() {
    var selectedLine: FileLine? = null
    val importButton: ButtonComponent = Components.button(Text.literal("Import")) {
        onFunctionUsed("import_file")
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
                child(Components.button(Text.literal(type.displayName)) {
                    onFunctionUsed("select_${type.name.lowercase()}_importScreen")
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
                    fileType.addChildren(this@SelectionImportScreen, this)
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
        vararg tooltips: String,
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
            left.child(
                Components.label(Text.literal(file.name.substring(0, file.name.lastIndexOf(".")))).apply {
                    tooltips.let { it.forEach { s -> tooltip(Text.literal(s)) } }
                }
            )
            center.child(Components.label(Text.literal("${"%.2f".format((file.length() / 1024.0))}KB")))
            right.child(Components.label(Text.literal(file.lastModified().formatToDate())))
        }
    }

    fun Long.formatToDate(): String {
        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }

    enum class Type(val displayName: String) {
        StructureBlock("Structure Block") {
            override fun addChildren(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File("saves").mkdirs()
                File("saves").listFiles()!!.asSequence()
                    .filter { it.isDirectory && it.resolve("generated").exists() }
                    .forEach { file ->
                        file.resolve("generated").getFiles().forEach {
                            rootComponent.child(screen.FileLine(it, it.path.simplifyPath()))
                        }
                    }
            }

            override fun import(file: File): Boolean {
                TODO()
            }

            private fun File.getFiles(): List<File> {
                val files = mutableListOf<File>()

                val path = Paths.get(path)

                walkFileTree(
                    path,
                    setOf(FileVisitOption.FOLLOW_LINKS),
                    Int.MAX_VALUE,
                    object : SimpleFileVisitor<Path>() {
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            if (!file.toFile().isDirectory && file.toString().endsWith(".nbt")) {
                                files.add(file.toFile())
                            }
                            return FileVisitResult.CONTINUE
                        }

                        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                            return FileVisitResult.CONTINUE
                        }
                    }
                )

                return files
            }

            private fun String.simplifyPath(): String {
                val split = split("\\")
                if (split.size < 4) return this
                return buildString {
                    append(split[1])
                    append("/")
                    append(split[3])
                    for (i in 4 until split.size) {
                        if (i == split.size - 1) {
                            append(":")
                            append(split[i].replace(".nbt", ""))
                        } else {
                            append("/")
                            append(split[i])
                        }
                    }
                }
            }
        },
        Litematica("Litematica") {
            override fun addChildren(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File("schematics").mkdirs()
                File("schematics").listFiles()!!.asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(".litematic") }
                    .forEach { rootComponent.child(screen.FileLine(it)) }
            }

            override fun import(file: File): Boolean {
                TODO()
            }
        },
        Other("Other") {
            override fun addChildren(screen: SelectionImportScreen, rootComponent: FlowLayout) {
                File("schematics").mkdirs()
                File("schematics").listFiles()!!.asSequence()
                    .filter { !it.isDirectory && (it.name.endsWith(".schematic") || it.name.endsWith(".schem")) }
                    .forEach { rootComponent.child(screen.FileLine(it)) }
            }

            override fun import(file: File): Boolean {
                TODO()
            }
        };

        abstract fun addChildren(screen: SelectionImportScreen, rootComponent: FlowLayout)
        abstract fun import(file: File): Boolean
    }
}