package com.github.zly2006.reden.mixin.noTimeOut;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ReadTimeoutHandler.class, remap = false)
public abstract class MixinReadTimeoutHandler {
    @Shadow protected abstract void readTimedOut(ChannelHandlerContext ctx) throws Exception;

    @Redirect(
            method = "channelIdle",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/handler/timeout/ReadTimeoutHandler;readTimedOut(Lio/netty/channel/ChannelHandlerContext;)V"
            ),
            remap = false
    )
    private void onTimeOut(ReadTimeoutHandler handler, io.netty.channel.ChannelHandlerContext ctx) throws Exception {
        if (!MalilibSettingsKt.NO_TIME_OUT.getBooleanValue()) {
            readTimedOut(ctx); // call original method
        }
    }
}
