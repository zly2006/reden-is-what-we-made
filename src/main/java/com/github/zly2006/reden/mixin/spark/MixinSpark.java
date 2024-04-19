package com.github.zly2006.reden.mixin.spark;

import com.github.zly2006.reden.mixinhelper.SparkHelper;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.CommandResponseHandler;
import me.lucko.spark.common.command.modules.SamplerModule;
import me.lucko.spark.common.sampler.Sampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SamplerModule.class)
public class MixinSpark {
    @Inject(
            method = "handleUpload",
            at = @At("HEAD")
    )
    private void onUpload(SparkPlatform platform, CommandResponseHandler resp, Sampler sampler, Sampler.ExportProps exportProps, boolean saveToFileFlag, CallbackInfo ci) {
        SparkHelper.sampler = sampler;
    }
}
