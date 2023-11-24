package com.github.zly2006.reden;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class Sounds {
    public final static SoundEvent THE_WORLD = SoundEvent.of(Reden.identifier("the_world"));
    public final static SoundEvent MIKU_MIKU = SoundEvent.of(Reden.identifier("miku_miku"));
    public final static SoundEvent FUWAFUWA_TIME = SoundEvent.of(Reden.identifier("fuwafuwa-time"));
    public final static SoundEvent TENSHI_NI_FURETAYO = SoundEvent.of(Reden.identifier("tenshi-ni-furetayo"));
    public static void init() {
        Registry.register(Registries.SOUND_EVENT, THE_WORLD.getId(), THE_WORLD);
        Registry.register(Registries.SOUND_EVENT, MIKU_MIKU.getId(), MIKU_MIKU);
        Registry.register(Registries.SOUND_EVENT, FUWAFUWA_TIME.getId(), FUWAFUWA_TIME);
        Registry.register(Registries.SOUND_EVENT, TENSHI_NI_FURETAYO.getId(), TENSHI_NI_FURETAYO);
    }
}
