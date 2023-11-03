package com.github.zly2006.reden;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class Sounds {
    public final static SoundEvent THE_WORLD = SoundEvent.of(Reden.identifier("the_world"));
    public static void init() {
        Registry.register(Registries.SOUND_EVENT, THE_WORLD.getId(), THE_WORLD);
    }
}
