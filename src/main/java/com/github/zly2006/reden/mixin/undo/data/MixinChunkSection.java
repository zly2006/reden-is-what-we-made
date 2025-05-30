package com.github.zly2006.reden.mixin.undo.data;

import com.github.zly2006.reden.access.ChunkSectionInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunkSection.class)
public class MixinChunkSection implements ChunkSectionInterface {
    @Unique int[] modifyTime;

    @Unique private int getIndex(int x, int y, int z) {
        return x << 8 | z << 4 | y;
    }

    @Override
    public int getModifyTime$reden(@NotNull BlockPos pos) {
        if (modifyTime == null) {
            return 0; // No modification time recorded
        }
        int index = getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        return modifyTime[index];
    }

    public void setModifyTime$reden(@NotNull BlockPos pos, int time) {
        if (modifyTime == null) {
            modifyTime = new int[16 * 16 * 16];
        }
        int index = getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        modifyTime[index] = time;
    }
}
