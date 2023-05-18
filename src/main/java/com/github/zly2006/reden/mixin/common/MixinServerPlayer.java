package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.PlayerPatchesView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayer implements PlayerPatchesView {
    List<Map<BlockPos, Entry>> blocks = new ArrayList<>();
    boolean recording;
    @NotNull
    @Override
    public List<Map<BlockPos, Entry>> getBlocks() {
        return blocks;
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public void setRecording(boolean b) {
        recording = b;
    }

    @Override
    public void stopRecording() {
        PlayerPatchesView.DefaultImpls.stopRecording(this);
    }
}
