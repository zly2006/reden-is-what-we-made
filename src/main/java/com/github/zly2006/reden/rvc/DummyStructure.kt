package com.github.zly2006.reden.rvc

import net.minecraft.block.BarrelBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BrushableBlock
import net.minecraft.block.DispenserBlock
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.block.entity.BeehiveBlockEntity
import net.minecraft.block.entity.BlastFurnaceBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.BrushableBlockEntity
import net.minecraft.block.entity.CampfireBlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.CommandBlockBlockEntity
import net.minecraft.block.entity.ComparatorBlockEntity
import net.minecraft.block.entity.ConduitBlockEntity
import net.minecraft.block.entity.DaylightDetectorBlockEntity
import net.minecraft.block.entity.DecoratedPotBlockEntity
import net.minecraft.block.entity.DispenserBlockEntity
import net.minecraft.block.entity.DropperBlockEntity
import net.minecraft.block.entity.FurnaceBlockEntity
import net.minecraft.block.entity.JigsawBlockEntity
import net.minecraft.block.entity.LecternBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.block.entity.SmokerBlockEntity
import net.minecraft.block.entity.TrappedChestBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path

open class DummyStructure(
    final override var name: String,
    private var pXSize: Int = 0,
    private var pYSize: Int = 0,
    private var pZSize: Int = 0
) : IStructure {
    final override val xSize: Int
        get() = pXSize
    final override val ySize: Int
        get() = pYSize
    final override val zSize: Int
        get() = pZSize

    private var pBlockstateMap: MutableMap<BlockPos, BlockState> = mutableMapOf()
    private var pBlockEntitiesMap: MutableMap<BlockPos, BlockEntity> = mutableMapOf()
    private var pEntities: MutableList<Entity> = mutableListOf()

    // Dummy.
    override fun save(path: Path) {}

    // Dummy.
    override fun load(path: Path) {}

    final override fun isInArea(pos: BlockPos): Boolean =
        pos.x in 0..(xSize-1) &&
        pos.y in 0..(ySize-1) &&
        pos.z in 0..(zSize-1)

    final override fun createPlacement(world: World, origin: BlockPos): IPlacement = object : IPlacement {
        override var name: String = this@DummyStructure.name
        override var enabled: Boolean = true
        override val structure: IStructure
            get() = this@DummyStructure
        override val world: World
            get() = world
        override val origin: BlockPos
            get() = origin

        override fun clearArea() {
            TODO("Not yet implemented")
        }

        override fun paste() {
            TODO("Not yet implemented")
        }
    }

    override final fun getBlockState(pos: BlockPos): BlockState = pBlockstateMap[pos] ?: Blocks.AIR.defaultState

    override final fun getBlockEntityData(pos: BlockPos): NbtCompound? = pBlockEntitiesMap[pos]?.createNbt()

    override final fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound = pBlockEntitiesMap[pos]?.createNbt() ?: run {
        val blockState = getBlockState(pos)
        pBlockEntitiesMap[pos] = when (blockState.block) {
            Blocks.CHEST -> ChestBlockEntity(pos, blockState)
            Blocks.SHULKER_BOX -> ShulkerBoxBlockEntity(pos, blockState)
            Blocks.TRAPPED_CHEST -> TrappedChestBlockEntity(pos, blockState)
            Blocks.BEEHIVE -> BeehiveBlockEntity(pos, blockState)
            Blocks.FURNACE -> FurnaceBlockEntity(pos, blockState)
            Blocks.BLAST_FURNACE -> BlastFurnaceBlockEntity(pos, blockState)
            Blocks.SMOKER -> SmokerBlockEntity(pos, blockState)
            Blocks.DISPENSER -> DispenserBlockEntity(pos, blockState)
            Blocks.DROPPER -> DropperBlockEntity(pos, blockState)
            Blocks.BARREL -> BarrelBlockEntity(pos, blockState)
            Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE -> CampfireBlockEntity(pos, blockState)
            Blocks.LECTERN -> LecternBlockEntity(pos, blockState)
            Blocks.BEACON -> BeaconBlockEntity(pos, blockState)
            Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK -> CommandBlockBlockEntity(pos, blockState)
            Blocks.JIGSAW -> JigsawBlockEntity(pos, blockState)
            Blocks.DAYLIGHT_DETECTOR -> DaylightDetectorBlockEntity(pos, blockState)
            Blocks.COMPARATOR -> ComparatorBlockEntity(pos, blockState)
            Blocks.CONDUIT -> ConduitBlockEntity(pos, blockState)
            Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL -> BrushableBlockEntity(pos, blockState)
            Blocks.DECORATED_POT -> DecoratedPotBlockEntity(pos, blockState)
            else -> throw IllegalArgumentException("Cannot resolve the block entity's type for the block at $pos! ")
        }
        pBlockEntitiesMap[pos]!!.createNbt()
    }

    override fun getEntities(): List<NbtCompound> = pEntities.map {
        var nbt = NbtCompound()
        it.writeNbt(nbt)
        nbt
    }
}