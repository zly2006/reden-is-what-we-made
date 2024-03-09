package com.github.zly2006.reden

import com.github.zly2006.reden.rvc.CuboidStructure
import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.blockPos
import com.github.zly2006.reden.rvc.io.LitematicaIO
import com.github.zly2006.reden.rvc.tracking.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.WorldInfo
import com.github.zly2006.reden.utils.ResourceLoader
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import net.minecraft.block.Blocks
import net.minecraft.network.NetworkSide
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.io.path.writeBytes

class LitematicaIOTest {
    private val file = ResourceLoader.loadBytes("schematics/8gt_Multi_Box_Sorter_Testing.litematic")!!

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            Path("run/rvc/test").toFile().deleteRecursively()
            setupMinecraftRegistries()
        }
    }

    @Test
    fun testImport_Blocks() {
        val file = Path("run/rvc/test/schematics/test.litematic").createParentDirectories()
        file.writeBytes(this.file)
        val litematica = LitematicaSchematic.createFromFile(file.parent.toFile(), file.name)!!
        val repository = RvcRepository.create("test", WorldInfo(), NetworkSide.CLIENTBOUND)
        val structure = TrackedStructure("test", repository)
        LitematicaIO.load(file, structure)
        RvcFileIO.save(file, structure)

        assert(structure.blocks.size == litematica.metadata.totalBlocks) {
            "Expected ${litematica.metadata.totalBlocks} blocks, got ${structure.blocks.size}"
        }
    }

    @Test
    fun testExport_Block() {
        val file = Path("run/rvc/test/schematics/test.litematic").createParentDirectories()
        val structure = CuboidStructure("test")
        for (i in 0..<100) {
            structure.blocks[RelativeCoordinate(i, i, i)] = Blocks.ALLIUM.defaultState
        }
        LitematicaIO.save(file.parent, structure)
        val litematica = LitematicaSchematic.createFromFile(file.parent.toFile(), file.name)!!

        structure.blocks.forEach { (coord, state) ->
            val pos = coord.blockPos(BlockPos.ORIGIN)
            val got = litematica.getSubRegionContainer("RVC Structure")!!.get(pos.x, pos.y, pos.z)
            assert(got == state) {
                "Expected $state at $pos, got $got"
            }
        }
    }

    //@Test
    fun testExport_TrackedStructure() {
        TODO()
    }
}
