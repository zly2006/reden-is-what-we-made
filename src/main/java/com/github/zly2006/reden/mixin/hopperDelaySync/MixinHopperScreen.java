package com.github.zly2006.reden.mixin.hopperDelaySync;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.TransferCooldownAccess;
import com.github.zly2006.reden.network.HopperCDSync;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperScreen.class)
public abstract class MixinHopperScreen extends HandledScreen<HopperScreenHandler> implements TransferCooldownAccess {
    @Unique Text originalTitle;
    @Unique private int transferCooldown;

    @Override
    public void setTransferCooldown$reden(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    @Override
    public int getTransferCooldown$reden() {
        return transferCooldown;
    }

    public MixinHopperScreen(HopperScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private boolean hopperCDEnabled() {
        ServerData data = ServerData.Companion.getServerData(MinecraftClient.getInstance());
        return data != null && data.getFeatureSet().contains("hopper-cd");
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void sendReqOnInit(HopperScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        if (hopperCDEnabled()) {
            ClientPlayNetworking.send(HopperCDSync.clientQueryPacket());
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void renderCD(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        originalTitle = title;
        if (hopperCDEnabled()) {
            title = originalTitle.copy().append(" (CD: ").append(String.valueOf(transferCooldown)).append(")");
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderCDAfter(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        title = originalTitle;
    }
}
