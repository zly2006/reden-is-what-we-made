package com.github.zly2006.reden.report

import com.github.zly2006.reden.transformers.RedenMixinExtension
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot
import org.spongepowered.asm.mixin.transformer.IMixinTransformer
import org.spongepowered.asm.mixin.transformer.ext.Extensions


class MyMixinPlugin: RestrictiveMixinConfigPlugin() {
    init {
        val mGetTransformer = MixinServiceKnot::class.java.getDeclaredMethod("getTransformer")
        mGetTransformer.setAccessible(true)
        val transformer = mGetTransformer.invoke(null) as IMixinTransformer
        (transformer.extensions as Extensions).add(RedenMixinExtension())
    }
    override fun getRefMapperConfig() = null

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun getMixins() = null
}
