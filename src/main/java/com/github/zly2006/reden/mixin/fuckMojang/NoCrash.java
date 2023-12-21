package com.github.zly2006.reden.mixin.fuckMojang;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = MinecraftClient.class, priority = 1500)
public abstract class NoCrash extends ReentrantThreadExecutor<Runnable> {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    public NoCrash(String string) {
        super(string);
    }

    @Redirect(
            method = "printCrashReport(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;Lnet/minecraft/util/crash/CrashReport;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/System;exit(I)V"
            ),
            require = 0 // Dont panic on mixin conflict!
    )
    private static void noExit(int status) {

    }
}
