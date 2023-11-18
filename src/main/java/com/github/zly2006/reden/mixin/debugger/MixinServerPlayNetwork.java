package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ServerPlayNetworkHandler.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinServerPlayNetwork {
}
