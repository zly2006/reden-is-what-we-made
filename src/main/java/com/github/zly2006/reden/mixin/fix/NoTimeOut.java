package com.github.zly2006.reden.mixin.fix;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class NoTimeOut {
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void onTimeout(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {

    }
}
