package com.github.zly2006.reden.access;

import net.minecraft.util.math.BlockPos;

public interface ChunkSectionInterface {
    int getModifyTime$reden(BlockPos pos);
    void setModifyTime$reden(BlockPos pos, int time);
}
