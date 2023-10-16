package com.github.zly2006.reden.report

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin

class MyMixinPlugin: RestrictiveMixinConfigPlugin() {
    init {
    }
    override fun getRefMapperConfig() = null

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        return super.shouldApplyMixin(targetClassName, mixinClassName)
    }

    override fun getMixins() = null
}
