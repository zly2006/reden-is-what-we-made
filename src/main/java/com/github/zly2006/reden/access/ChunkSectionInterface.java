package com.github.zly2006.reden.access;

import net.minecraft.util.math.BlockPos;

public interface ChunkSectionInterface {
    int getModifyTime(BlockPos pos);
    void setModifyTime(BlockPos pos, int time);
}
