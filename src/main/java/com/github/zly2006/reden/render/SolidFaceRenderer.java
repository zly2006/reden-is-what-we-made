package com.github.zly2006.reden.render;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SolidFaceRenderer {
    private Vec3d[] cachedVertexes = null;

    public SolidFaceRenderer() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register((ctx) ->
                this.render(ctx.camera().getPos(), ctx.matrixStack().peek().getPositionMatrix()));
    }

    public void tick() {
        if(!MalilibSettingsKt.SOLID_FACE_RENDERER.getBooleanValue()) {
            this.cachedVertexes = null;
            return;
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) {
            return;
        }
        BlockHitResult bhr = this.raycast(player);
        if(bhr.getType() == HitResult.Type.BLOCK) {
            BlockState state = player.getWorld().getBlockState(bhr.getBlockPos());
            IConfigOptionListEntry type = MalilibSettingsKt.SOLID_FACE_SHAPE_PREDICATE.getOptionListValue();
            SideShapeType pred = ((ShapePredicateOptionEntry) type).getPredicate();
            if(state.isSideSolid(player.getWorld(), bhr.getBlockPos(), bhr.getSide(), pred)) {
                this.updateVertexes(bhr);
            } else {
                this.cachedVertexes = null;
            }
        } else {
            this.cachedVertexes = null;
        }
    }

    private BlockHitResult raycast(ClientPlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVec(0).multiply(5.0));
        if(player.getWorld() == null){
            Vec3d deltaReversed = start.subtract(end);
            return BlockHitResult.createMissed(end, Direction.getFacing(deltaReversed.x, deltaReversed.y, deltaReversed.z),
                    BlockPos.ofFloored(end));
        }

        return player.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player));
    }

    private void updateVertexes(BlockHitResult bhr) {
        BlockPos pos = bhr.getBlockPos();
        Direction dir = bhr.getSide();
        Vec3d[] vertexes = new Vec3d[4];
        Vector3f dirOffset = dir.getUnitVector().mul(0.51F);
        Vec3d center = pos.toCenterPos()
                .add(dirOffset.x, dirOffset.y, dirOffset.z);
        switch (dir.getAxis()) {
            case X -> {
                vertexes[0] = center.add(0.0, 0.48, 0.48);
                vertexes[1] = center.add(0.0, 0.48, -0.48);
                vertexes[2] = center.add(0.0, -0.48, -0.48);
                vertexes[3] = center.add(0.0, -0.48, 0.48);
            }
            case Y -> {
                vertexes[0] = center.add(0.48, 0.0, 0.48);
                vertexes[1] = center.add(0.48, 0.0, -0.48);
                vertexes[2] = center.add(-0.48, 0.0, -0.48);
                vertexes[3] = center.add(-0.48, 0.0, 0.48);
            }
            case Z -> {
                vertexes[0] = center.add(0.48, 0.48, 0.0);
                vertexes[1] = center.add(0.48, -0.48, 0.0);
                vertexes[2] = center.add(-0.48, -0.48, 0.0);
                vertexes[3] = center.add(-0.48, 0.48, 0.0);
            }
        }

        this.cachedVertexes = vertexes;
    }

    private void render(Vec3d cameraPos, Matrix4f positionMatrix) {
        if(this.cachedVertexes != null && MalilibSettingsKt.SOLID_FACE_RENDERER.getBooleanValue()) {
            RenderSystem.disableCull();
            RenderSystem.disableScissor();
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            float prevLineWidth = RenderSystem.getShaderLineWidth();
            RenderSystem.lineWidth(3.0F);
            BufferBuilder bb = Tessellator.getInstance().getBuffer();
            bb.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for(int i = 0; i < 4; i++) {
                Vector3f p1 = this.cachedVertexes[i].subtract(cameraPos).toVector3f();
                Vector3f p2 = this.cachedVertexes[(i + 1) % 4].subtract(cameraPos).toVector3f();
                bb.vertex(positionMatrix, p1.x, p1.y, p1.z).color(0xFF, 0, 0, 0xFF).next();
                bb.vertex(positionMatrix, p2.x, p2.y, p2.z).color(0xFF, 0, 0, 0xFF).next();
            }

            Tessellator.getInstance().draw();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.lineWidth(prevLineWidth);
        }
    }

    public enum ShapePredicateOptionEntry implements IConfigOptionListEntry {
        FULL(0, SideShapeType.FULL),
        CENTER(1, SideShapeType.CENTER),
        RIGID(2, SideShapeType.RIGID);

        private final int id;
        private final SideShapeType shapeType;

        ShapePredicateOptionEntry(int id, SideShapeType type) {
            this.id = id;
            this.shapeType = type;
        }

        @Override
        public String getStringValue() {
            return this.name();
        }

        @Override
        public String getDisplayName() {
            return this.name();
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return values()[(this.id + (forward ? 1 : -1)) % values().length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            return valueOf(value);
        }

        public SideShapeType getPredicate() {
            return this.shapeType;
        }
    }
}
