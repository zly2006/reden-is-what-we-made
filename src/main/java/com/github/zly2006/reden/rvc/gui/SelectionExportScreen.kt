package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.rvc.tracking.RvcRepository
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text
import java.nio.file.Path

class SelectionExportScreen(
    val rvc: RvcRepository
): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.LEFT)
            .verticalAlignment(VerticalAlignment.TOP)
    }

    enum class ExportType(
        val displayName: Text,
        val hover: Text,
    ) {
//        StructureBlock,
//        Schematics,
//        Litematica,
//        LitematicaMultiBox,
//        RVCArchive
        ;

        /**
         * @param path the path to export to, for most types it should `./schematics`,
         *   or for structure block it should be the save directory
         */
        abstract fun export(path: Path)
    }
}
