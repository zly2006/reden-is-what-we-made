package com.github.zly2006.reden.mixin.yo;

import com.github.zly2006.reden.Reden;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.OptionalLong;

@Mixin(CreateWorldScreen.class)
public class MixinCreateWorldScreen {
    @Shadow
    @Final
    WorldCreator worldCreator;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void modify(MinecraftClient client, Screen parent, GeneratorOptionsHolder generatorOptionsHolder, Optional defaultWorldType, OptionalLong seed, CallbackInfo ci) {
        if (Reden.isRedenDev()) {
            worldCreator.setGameMode(WorldCreator.Mode.CREATIVE);
            worldCreator.setCheatsEnabled(true);
            var registry = generatorOptionsHolder.getCombinedRegistryManager();
            var preset = registry.get(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET).get(Identifier.of("the_void"));
            assert preset != null;
            var chunkGenerator = new FlatChunkGenerator(preset.settings());
            worldCreator.setWorldType(new WorldCreator.WorldType(registry.get(RegistryKeys.WORLD_PRESET).getEntry(WorldPresets.FLAT).get()));
            worldCreator.applyModifier((dynamicRegistryManager, registryHolder) -> registryHolder.with(dynamicRegistryManager, chunkGenerator));
        }
    }
}
