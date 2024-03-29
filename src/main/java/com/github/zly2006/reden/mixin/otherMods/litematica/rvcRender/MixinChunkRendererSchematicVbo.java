package com.github.zly2006.reden.mixin.otherMods.litematica.rvcRender;

import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask;
import fi.dy.masa.litematica.render.schematic.ChunkRendererSchematicVbo;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.IntBoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ChunkRendererSchematicVbo.class, remap = false)
public class MixinChunkRendererSchematicVbo {
    @Shadow
    @Final
    protected List<IntBoundingBox> boxes;

    @Shadow
    @Final
    protected BlockPos.Mutable position;

    @Shadow
    protected volatile WorldSchematic world;

    @Inject(
            method = "rebuildWorldView",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;clear()V",
                    shift = At.Shift.AFTER
            )
    )
    private void addRedenBox(CallbackInfo ci) {
        RvcMoveStructureLitematicaTask litematicaTask = RvcMoveStructureLitematicaTask.stackTop();
        if (litematicaTask == null) return;
        BlockPos startPos = new ChunkPos(position).getStartPos();
        IntBoundingBox box = litematicaTask.getBox();
        if (box == null) return;
        if (box.maxX >= startPos.getX() && box.minX < startPos.getX() + 16 &&
                box.maxZ >= startPos.getZ() && box.minZ < startPos.getZ() + 16) {
            IntBoundingBox box1 = new IntBoundingBox(
                    Math.max(box.minX, startPos.getX()),
                    box.minY,
                    Math.max(box.minZ, startPos.getZ()),
                    Math.min(box.maxX, startPos.getX() + 15),
                    box.maxY,
                    Math.min(box.maxZ, startPos.getZ() + 15)
            );
            boxes.add(box1);
        }
    }
}
