package com.github.zly2006.reden.transformers;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        try {
            Method mGetTransformer = MixinServiceKnot.class.getDeclaredMethod("getTransformer");
            mGetTransformer.setAccessible(true);
            IMixinTransformer transformer = (IMixinTransformer) mGetTransformer.invoke(null);
            IExtensionRegistry extReg = transformer.getExtensions();
            if (extReg instanceof Extensions extensions) {
                Field fActiveExtensions = Extensions.class.getDeclaredField("activeExtensions");
                fActiveExtensions.setAccessible(true);
                fActiveExtensions.set(extensions, new ArrayList<>(extensions.getActiveExtensions()));
                // force add my extension
                RedenMixinExtension myExtension = new RedenMixinExtension();
                extensions.add(myExtension);
                extensions.getActiveExtensions().add(myExtension);
            }
            // then load our transformed classes
            ServerWorld.class.getName();
            MinecraftServer.class.getName();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
