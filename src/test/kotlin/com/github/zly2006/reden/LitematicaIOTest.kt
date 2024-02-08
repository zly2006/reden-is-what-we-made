package com.github.zly2006.reden

import com.github.zly2006.reden.rvc.io.LitematicaIO
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo
import com.github.zly2006.reden.utils.ResourceLoader
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import net.minecraft.network.NetworkSide
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.io.path.writeBytes

class LitematicaIOTest {
    private val file = ResourceLoader.loadBytes("schematics/8gt_Multi_Box_Sorter_Testing.litematic")!!

    @Test
    fun testImport_Blocks() {
        setupMinecraftRegistries()
        val file = Path("run", "rvc", "test", "schematics", "test.litematic").createParentDirectories()
        file.writeBytes(this.file)
        val litematica = LitematicaSchematic.createFromFile(file.parent.toFile(), file.name)!!
        val repository = RvcRepository.create("test", WorldInfo(), NetworkSide.CLIENTBOUND)
        val structure = TrackedStructure("test", NetworkSide.CLIENTBOUND)
        LitematicaIO.load(file, structure)
        repository.commit(structure, "import", null)

        assert(structure.blocks.size == litematica.metadata.totalBlocks)
    }

    //    @Test
    fun testExport_Block() {
        TODO()
    }
}
