package com.github.zly2006.reden.mixin.otherMods.tweeakeroo.hopperDelaySync;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.network.HopperCDSync;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = RenderUtils.class, remap = false)
public class MixinX {
    @Unique private static int tickCount;
    @Inject(
            method = "renderInventoryOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/render/InventoryOverlay;renderInventoryBackground(Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;IIIILnet/minecraft/client/MinecraftClient;)V",
                    ordinal = 1,
                    remap = true
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void capture(MinecraftClient mc, DrawContext drawContext, CallbackInfo ci, World world, Entity cameraEntity, HitResult trace, Inventory inv, ShulkerBoxBlock block, LivingEntity entityLivingBase, int xCenter, int yCenter, int x, int y, boolean isHorse, int totalSlots, int firstSlot, InventoryOverlay.InventoryRenderType type, InventoryOverlay.InventoryProperties props, int rows, int xInv, int yInv) {
        tickCount++;
        if (type == InventoryOverlay.InventoryRenderType.HOPPER) {
            ServerData data = ServerData.Companion.serverData(mc);
            if (data != null && data.getFeatureSet().contains("hopper-cd")) {
                BlockPos pos = ((BlockHitResult) trace).getBlockPos();
                if (pos.equals(HopperCDSync.Companion.getCurrentPos())) {
                    String text = "CD: " + HopperCDSync.Companion.getCurrentDelay();
                    drawContext.drawText(mc.textRenderer, text, xInv + 4, yInv - 10, 0xffffff, false);
                }
                if (tickCount % 10 == 0) {
                    ClientPlayNetworking.send(HopperCDSync.clientQueryPacket(pos));
                }
            }
        }
    }
}
