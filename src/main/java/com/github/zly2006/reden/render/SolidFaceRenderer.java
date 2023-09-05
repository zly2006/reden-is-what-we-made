package com.github.zly2006.reden.render;

import carpet.script.utils.ShapeDispatcher;
import carpet.script.value.ListValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.RaycastContext;

import java.util.Collections;
import java.util.Map;

public class SolidFaceRenderer {
    private final ServerPlayerEntity player;

    public SolidFaceRenderer(ServerPlayerEntity player) {
        this.player = player;
    }

    public void tick() {
        BlockHitResult bhr = this.raycast();
        if(bhr.getType() == HitResult.Type.BLOCK) {
            BlockState state = this.player.getWorld().getBlockState(bhr.getBlockPos());
            if(state.isSideSolidFullSquare(this.player.getWorld(), bhr.getBlockPos(), bhr.getSide())) {
                ShapeDispatcher.sendShape(Collections.singleton(this.player),
                        Collections.singletonList(this.shapeFor(bhr)));
            }
        }
    }

    private BlockHitResult raycast() {
        Vec3d start = this.player.getEyePos();
        Vec3d end = start.add(this.player.getRotationVec(0).multiply(5.0));
        return this.player.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, null));
    }

    private ShapeDispatcher.ShapeWithConfig shapeFor(BlockHitResult bhr) {
        BlockPos pos  = bhr.getBlockPos();
        Box box = (switch (bhr.getSide()) {
            case EAST -> new Box(1.01, 0, 0, 1.01, 1, 1);
            case WEST -> new Box(-0.01, 0, 0, -0.01, 1, 1);
            case SOUTH -> new Box(0, 0, 1.01, 1, 1, 1.01);
            case NORTH -> new Box(0, 0, -0.01, 1, 1, -0.01);
            case UP -> new Box(0, 1.01, 0, 1, 1.01, 1);
            case DOWN -> new Box(0, -0.01, 0, 1, -0.01, 1);
        }).offset(pos.getX(), pos.getY(), pos.getZ());
        Map<String, Value> params = Maps.newHashMap();
        params.put("from", ListValue.fromTriple(box.minX, box.minY, box.minZ));
        params.put("to", ListValue.fromTriple(box.maxX, box.maxY, box.maxZ));
        params.put("color", NumericValue.of(0xFF0000FF));
        params.put("fill", NumericValue.of(0));
        params.put("duration", NumericValue.of(2));
        params.put("dim", StringValue.of(this.player.getWorld().getDimensionKey().getValue().toString()));
        ShapeDispatcher.ExpiringShape shape = ShapeDispatcher.create(this.player.getServer(), "box", params);
        return new ShapeDispatcher.ShapeWithConfig(shape, params);
    }
}
