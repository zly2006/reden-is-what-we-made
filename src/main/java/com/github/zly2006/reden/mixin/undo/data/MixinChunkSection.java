package com.github.zly2006.reden.mixin.undo.data;

import com.github.zly2006.reden.access.ChunkSectionInterface;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkSection.class)
public class MixinChunkSection implements ChunkSectionInterface {
    @Unique int[] modifyTime;

    @Unique private int getIndex(int x, int y, int z) {
        if (modifyTime == null) {
            modifyTime = new int[16 * 16 * 16];
        }
        return x << 8 | z << 4 | y;
    }

    @Override
    public int getModifyTime(@NotNull BlockPos pos) {
        int index = getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        return modifyTime[index];
    }

    @Override
    public void setModifyTime(@NotNull BlockPos pos, int time) {
        int index = getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        modifyTime[index] = time;
    }
}
