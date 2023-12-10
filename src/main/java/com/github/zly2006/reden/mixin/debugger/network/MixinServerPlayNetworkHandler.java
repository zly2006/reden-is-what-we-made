package com.github.zly2006.reden.mixin.debugger.network;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.stages.TickStageWorldProvider;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "method_44356",
            at = @At("HEAD")
    )
    private void startCommandExecute(CommandExecutionC2SPacket commandExecutionC2SPacket, Optional<?> optional, CallbackInfo ci) {
        ServerData data = data(player.server);
        ServerWorld world = player.getServerWorld();
        data.getTickStageTree().insert2child(new TickStageWorldProvider("commands_stage", data.getTickStageTree().peekLeaf(), world));
        data.getTickStageTree().next().tick();
    }
}
