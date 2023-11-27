package com.github.zly2006.reden.mixin.yo;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerListS2CPacket.Action.class)
public class MixinPlayerList {
}
