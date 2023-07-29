package com.github.zly2006.reden

import net.minecraft.util.math.BlockPos

fun MutableMap<BlockPos, Int>.testMapGet(x: Int, y: Int, z: Int) {
    Reden.LOGGER.info("map.get(BlockPos($x, $y, $z)): ${get(BlockPos(x, y, z))}")
}
fun mapTest0() {
    val map: MutableMap<BlockPos, Int> = mutableMapOf()
    map.put(BlockPos(1, 1, 1), 1)
    map.put(BlockPos(2, 1, 4), 14)
    map.put(BlockPos(2008, 2, 1), 5)
    map.put(BlockPos(2008, 1, 13), 14)
    map.testMapGet(1, 1, 1)
    map.testMapGet(2, 1, 4)
    map.testMapGet(2008, 2, 1)
    map.testMapGet(2008, 1, 3)
}
