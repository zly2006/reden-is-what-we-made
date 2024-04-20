package com.github.zly2006.reden.mixin.spark;

import com.github.zly2006.reden.mixinhelper.SparkHelper;
import com.llamalad7.mixinextras.sugar.Local;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.CommandResponseHandler;
import me.lucko.spark.common.command.modules.SamplerModule;
import me.lucko.spark.common.sampler.Sampler;
import me.lucko.spark.proto.SparkSamplerProtos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SamplerModule.class, remap = false)
public class MixinSpark {
    @Inject(
            method = "handleUpload",
            at = @At("RETURN"),
            remap = false
    )
    private void onUpload(SparkPlatform platform, CommandResponseHandler resp, Sampler sampler, Sampler.ExportProps exportProps, boolean saveToFileFlag, CallbackInfo ci, @Local SparkSamplerProtos.SamplerData output) {
        SparkHelper.output = output;
    }
}
