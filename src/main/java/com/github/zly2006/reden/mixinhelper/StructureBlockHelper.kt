package com.github.zly2006.reden.mixinhelper

import net.minecraft.block.Blocks.STRUCTURE_BLOCK
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object StructureBlockHelper {
    val isValid: Boolean get() {
        val mc = MinecraftClient.getInstance()
        return mc.player?.isCreative == true && mc.world?.registryKey?.value == lastUsedWorld
                && mc.world?.getBlockState(lastUsed)?.block == STRUCTURE_BLOCK
    }
    var lastUsed: BlockPos? = null
    var lastUsedWorld: Identifier? = null

}
