package com.github.zly2006.reden.mixin.dynamicSound;

import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.Map;

@Mixin(SoundManager.class)
public class MixinSoundManager {
    @Shadow @Final private Map<Identifier, Resource> soundResources;

    @Shadow @Final private Map<Identifier, WeightedSoundSet> sounds;

    private void addSound(Identifier identifier, File file) {
        // todo
        throw new NotImplementedException();
    }
}
