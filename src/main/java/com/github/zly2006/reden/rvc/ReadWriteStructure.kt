package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.io.StructureIO
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import java.nio.file.Path
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function

abstract class ReadWriteStructure(override var name: String) : IWritableStructure {
    override var xSize: Int = 0; protected set
    override var ySize: Int = 0; protected set
    override var zSize: Int = 0; protected set
    protected var minX: Int = Int.MAX_VALUE
    protected var minY: Int = Int.MAX_VALUE
    protected var minZ: Int = Int.MAX_VALUE
    private fun checkSize(pos: RelativeCoordinate) {
        if (pos.x < minX) minX = pos.x
        if (pos.y < minY) minY = pos.y
        if (pos.z < minZ) minZ = pos.z
        if (xSize <= pos.x) xSize = pos.x + 1
        if (ySize <= pos.y) ySize = pos.y + 1
        if (zSize <= pos.z) zSize = pos.z + 1
    }
    protected var io: StructureIO? = null
    val blocks = RelativeCoordinateBlockHashMap()
    val blockEntities = mutableMapOf<RelativeCoordinate, NbtCompound>()
    override val entities = mutableMapOf<UUID, NbtCompound>()
    override fun setBlockState(pos: RelativeCoordinate, state: BlockState) { blocks[pos] = state }
    override fun getBlockState(pos: RelativeCoordinate) = blocks[pos] ?: Blocks.AIR.defaultState!!
    override fun getBlockEntityData(pos: RelativeCoordinate) = blockEntities[pos]
    override fun getOrCreateBlockEntityData(pos: RelativeCoordinate) = blockEntities.getOrPut(pos) { NbtCompound() }
    override fun setBlockEntityData(pos: RelativeCoordinate, nbt: NbtCompound) {
        blockEntities[pos] = nbt
    }
    override fun save(path: Path) { io?.save(path, this) }
    override fun load(path: Path) { io?.load(path, this) }
    override fun assign(another: IStructure) {
        // 看见没，什么叫高效
        if (another is ReadWriteStructure) {
            blocks.clear()
            blocks.putAll(another.blocks)
            blockEntities.clear()
            blockEntities.putAll(another.blockEntities)
            entities.clear()
            entities.putAll(another.entities)
        } else super.assign(another)
    }


    inner class RelativeCoordinateBlockHashMap : HashMap<RelativeCoordinate, BlockState>() {
        override fun put(key: RelativeCoordinate, value: BlockState): BlockState? {
            checkSize(key)
            return super.put(key, value)
        }

        override fun putAll(from: Map<out RelativeCoordinate, BlockState>) {
            from.keys.forEach(::checkSize)
            super.putAll(from)
        }

        override fun putIfAbsent(key: RelativeCoordinate, value: BlockState): BlockState? {
            checkSize(key)
            return super.putIfAbsent(key, value)
        }

        override fun compute(
            key: RelativeCoordinate,
            remappingFunction: BiFunction<in RelativeCoordinate, in BlockState?, out BlockState?>
        ): BlockState? {
            checkSize(key)
            return super.compute(key, remappingFunction)
        }

        override fun computeIfAbsent(
            key: RelativeCoordinate,
            mappingFunction: Function<in RelativeCoordinate, out BlockState>
        ): BlockState {
            checkSize(key)
            return super.computeIfAbsent(key, mappingFunction)
        }

        override fun computeIfPresent(
            key: RelativeCoordinate,
            remappingFunction: BiFunction<in RelativeCoordinate, in BlockState, out BlockState?>
        ): BlockState? {
            checkSize(key)
            return super.computeIfPresent(key, remappingFunction)
        }

        override fun remove(key: RelativeCoordinate, value: BlockState): Boolean {
            if (super.remove(key, value)) {
                if (key.x == xSize - 1 || key.y == ySize - 1 || key.z == zSize - 1) {
                    xSize = 0
                    ySize = 0
                    zSize = 0
                    forEach { (k, _) ->
                        if (k.x >= xSize) xSize = k.x + 1
                        if (k.y >= ySize) ySize = k.y + 1
                        if (k.z >= zSize) zSize = k.z + 1
                    }
                }
                if (key.x == minX || key.y == minY || key.z == minZ) {
                    minX = Int.MAX_VALUE
                    minY = Int.MAX_VALUE
                    minZ = Int.MAX_VALUE
                    forEach { (k, _) ->
                        if (k.x < minX) minX = k.x
                        if (k.y < minY) minY = k.y
                        if (k.z < minZ) minZ = k.z
                    }
                }
                return true
            }
            return false
        }

        override fun clear() {
            minX = Int.MAX_VALUE
            minY = Int.MAX_VALUE
            minZ = Int.MAX_VALUE
            xSize = 0
            ySize = 0
            zSize = 0
            super.clear()
        }
    }
}
