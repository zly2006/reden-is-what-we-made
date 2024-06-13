package com.github.zly2006.reden.mixin.debug.hdr;

import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J",
                    remap = false
            )
    )
    private void setHint(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, String videoMode, String title, CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_ACCUM_RED_BITS, 10);
        GLFW.glfwWindowHint(GLFW.GLFW_ACCUM_GREEN_BITS, 10);
        GLFW.glfwWindowHint(GLFW.GLFW_ACCUM_BLUE_BITS, 10);
    }
}
