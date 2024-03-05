package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.ModNames
import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.io.LitematicaIO
import com.github.zly2006.reden.rvc.io.SchematicIO
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextureComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute

private const val WIKI = "https://wiki.redenmc.com/RVC/导入导出#导出"

class SelectionExportScreen(
    val parent: Screen? = null,
    val rvc: RvcRepository,
) : BaseOwoScreen<FlowLayout>() {
    private var selectedType: ExportType = ExportType.Litematica
        set(value) {
            field = value
            extensionLabel.text(Text.literal(".${value.extension}"))
            onNameFieldChanged()

            val parent = optionsPanel?.parent() as? FlowLayout?
            optionsPanel?.remove()
            refreshOptions()
            parent?.child(optionsPanel!!)
        }
    private val statusLabel = Components.label(Text.literal("Export to ${selectedType.displayName.string} type"))
    private val extensionLabel = Components.label(Text.literal(".${selectedType.extension}"))
    private var optionsPanel: FlowLayout? = null
    private val exportButton: ButtonComponent = Components.button(Text.literal("Export")) {
        val timeStart = System.currentTimeMillis()
        val path = if (selectedType == ExportType.StructureBlock) {
            client!!.server?.session?.directory?.path
        } else Path(SelectionImportScreen.FOLDER_SCHEMATICS)
        if (path != null) {
            selectedType.export(path, rvc.head().apply {
                name = nameField.text
            })
            close()
            Reden.LOGGER.info("$selectedType Exported to ${path.absolute()} in ${System.currentTimeMillis() - timeStart}ms")
        }
    }
    private val nameField = Components.textBox(Sizing.fixed(100)).apply {
        onChanged().subscribe { content ->
            onNameFieldChanged(content)
            if (selectedLine != null && content != selectedLine!!.file.nameWithoutExtension) selectedLine = null
        }
        text(rvc.name)
    }

    private fun onNameFieldChanged(content: String = nameField.text) {
        File(SelectionImportScreen.FOLDER_SCHEMATICS).mkdirs()
        if (File(SelectionImportScreen.FOLDER_SCHEMATICS).listFiles()!!
                .any { it.name == "$content.${selectedType.extension}" }
        ) {
            statusLabel.text(Text.literal("This operation will overwrite the file with same name").red())
            exportButton.active(true)
        } else if (content.isEmpty()) {
            statusLabel.text(Text.literal("Name cannot be empty").red())
            exportButton.active(false)
        } else {
            statusLabel.text(Text.literal("Export to ${selectedType.displayName.string} type"))
            exportButton.active(true)
        }
    }

    lateinit var selectedButton: ButtonComponent
    var selectedLine: FileLine? = null
    private val multiBoxCheckBox: CheckboxComponent =
        Components.checkbox(ExportType.LitematicaMultiBox.displayName).apply {
        checked(false)
        onChanged {
            selectedType = if (it) {
//                statusLabel.text(Text.literal("Export to ${ExportType.LitematicaMultiBox.displayName.string} type"))
                ExportType.LitematicaMultiBox
            } else {
//                statusLabel.text(Text.literal("Export to ${ExportType.Litematica.displayName.string} type"))
                ExportType.Litematica
            }
            onNameFieldChanged()
        }
    }
    val multiBoxHelp: TextureComponent =
        Components.texture(Reden.identifier("help_icon.png"), 0, 0, 16, 16, 16, 16).apply {
            mouseDown().subscribe { _: Double, _: Double, b: Int ->
                if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                Util.getOperatingSystem().open(WIKI)
                    true
                } else false
        }
            cursorStyle(CursorStyle.HAND)
    }

    private fun refreshOptions() {
        optionsPanel = (Containers.verticalFlow(Sizing.fill(50), Sizing.fill()).apply {
            gap(5)
            child(Components.label(Text.literal(rvc.name)))
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                child(Components.label(Text.literal("Name:")))
                child(nameField)
                child(extensionLabel)
            })
            child(exportButton)
            child(statusLabel)
            if (selectedType == ExportType.LitematicaMultiBox || selectedType == ExportType.Litematica) {
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    gap(5)
                    child(multiBoxCheckBox)
                    child(multiBoxHelp)
                })
            }
        })
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
            .padding(Insets.of(5, 0, 5, 0))

        rootComponent.gap(5)

        rootComponent.child(
            Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                gap(5)
                alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                child(Components.label(Text.literal("Export RVC Structure to:")))
                ExportType.entries
                    // Note: it is the same format as [Litematica] mode
                    .filterNot { it == ExportType.LitematicaMultiBox }.forEach { type: ExportType ->
                        child(Components.button(type.displayName) {
                            it.active(false)
                            selectedButton.active(true)
                            selectedType = type
                            selectedButton = it
                            onNameFieldChanged()
                        }.apply {
                            tooltip(type.hover)
                            if (type == selectedType) {
                                selectedButton = this
                                selectedButton.active(false)
                            }
                        })
                    }
            }
        )

        rootComponent.child(Containers.horizontalFlow(Sizing.fill(), Sizing.fill(85)).apply {
            gap(5)
            alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            child(
                Containers.verticalScroll(
                    Sizing.fill(50),
                    Sizing.fill(),
                    Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
                        padding(Insets.vertical(5))
                        File(SelectionImportScreen.FOLDER_SCHEMATICS).mkdirs()
                        File(SelectionImportScreen.FOLDER_SCHEMATICS).listFiles()!!.asSequence()
                            .filter {
                                !it.isDirectory && it.extension in setOf(
                                    SelectionImportScreen.EXTENSION_LITEMATICA,
                                    SelectionImportScreen.EXTENSION_SCHEMATIC,
                                    SelectionImportScreen.EXTENSION_LITEMATICA
                                )
                            }
                            .forEach { child(FileLine(it, it.name)) }
                    }).apply {
                    scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                }
            )
            refreshOptions()
            child(optionsPanel)
        })
    }

    override fun close() {
        client!!.setScreen(parent)
    }

    inner class FileLine(
        val file: File,
        val name: String,
    ) : FlowLayout(Sizing.fill(), Sizing.content(), Algorithm.HORIZONTAL) {
        val content: FlowLayout = Containers.horizontalFlow(Sizing.fill(), Sizing.content(2))

        init {
            gap(5)
            content.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
            child(content)

            content.child(Components.label(Text.literal(" $name")))

            mouseDown().subscribe { _, _, _ ->
                selectedLine = this
                nameField.text(file.nameWithoutExtension)
                true
            }
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (selectedLine == this || hovered) {
                context.fill(x, y, x + width - 3, y + height, Color(255, 255, 255, (255 * 0.2).toInt()).rgb)
            }
            if (selectedLine == this) {
                context.drawRectOutline(x, y, width - 3, height, Color.WHITE.rgb)
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    enum class ExportType(
        val displayName: Text,
        val hover: Text,
        val extension: String,
    ) {
        StructureBlock(Text.literal("Structure Block"), Text.empty(), SelectionImportScreen.EXTENSION_NBT) {
            override fun export(path: Path, head: TrackedStructure) {
                val identifier = Identifier(head.name)
                TODO()
            }
        },
        Schematics(Text.literal("Schematics"), Text.empty(), SelectionImportScreen.EXTENSION_SCHEMATIC) {
            override fun export(path: Path, head: TrackedStructure) {
                SchematicIO.save(path, head)
            }
        },
        Litematica(ModNames.litematicaName, Text.empty(), SelectionImportScreen.EXTENSION_LITEMATICA) {
            override fun export(path: Path, head: TrackedStructure) {
                LitematicaIO.save(path, head, false)
            }
        },
        LitematicaMultiBox(
            Text.literal("Litematica Multi-Box"),
            Text.empty(),
            SelectionImportScreen.EXTENSION_LITEMATICA
        ) {
            override fun export(path: Path, head: TrackedStructure) {
                LitematicaIO.save(path, head, true)
            }
        },
        RVCArchive(Text.literal("RVC Archive"), Text.empty(), SelectionImportScreen.EXTENSION_RVC_ARCHIVE) {
            override fun export(path: Path, head: TrackedStructure) {
                TODO("Not yet implemented")
            }
        };

        abstract fun export(path: Path, head: TrackedStructure)
    }
}
