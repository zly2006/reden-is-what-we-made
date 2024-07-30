package com.github.zly2006.reden.mixin.debug;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonPart.class)
public abstract class MixinEnderPart extends Entity {
    public MixinEnderPart(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void baseTick() {
        if (UtilsKt.isClient() && MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue()) {
            System.out.println("AAAAAAA");
        }
        super.baseTick();
    }
}
