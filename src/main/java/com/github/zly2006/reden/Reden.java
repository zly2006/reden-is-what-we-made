package com.github.zly2006.reden;

import com.github.zly2006.reden.utils.UtilsKt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.zly2006.reden.network.ChannelsKt;

public class Reden implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("template");
    public static final String MOD_VERSION = /*$ mod_version*/ "0.10.1";
    public static final String MOD_ID = "reden";
    public static final String MOD_NAME = "Reden";

    @Override
    public void onInitialize() {
        ChannelsKt.registerChannelServer();
        ServerLifecycleEvents.SERVER_STARTED.register(UtilsKt::setServer);
    }

    private ClassLoader hijackClassLoader() {
        ClassLoader classLoader = Reden.class.getClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Reden's class loader is null");
        }
        return new ClassLoader(classLoader) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.startsWith("com.github.zly2006.reden")) {
                    try {
                        getClassLoadingLock("1").wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return super.loadClass(name);
                }
                return classLoader.loadClass(name);
            }
        };
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
