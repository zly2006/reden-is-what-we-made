package com.github.zly2006.reden.mixin.log;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientCoon {
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void onPacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (MalilibSettingsKt.debug()) {
            System.out.println("Client received packet: " + packet.getClass().getName());
        }
    }
}
