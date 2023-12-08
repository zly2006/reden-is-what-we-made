package com.github.zly2006.reden.mixin.log;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ClientConnection.class)
public class MixinClientCoon {
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void onPacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (MalilibSettingsKt.DEBUG_PACKET_LOGGER.getBooleanValue()) {
            var sb = new StringBuilder("Client received packet: " + packet.getClass().getName());
            for (Field field : packet.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    sb.append("; ").append(field.getName()).append(": ").append(StringUtils.abbreviate(String.valueOf(field.get(packet)), 100));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(sb);
        }
    }
}
