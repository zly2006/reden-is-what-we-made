package com.github.zly2006.reden.report

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin

class MyMixinPlugin: RestrictiveMixinConfigPlugin() {
    init {
    }
    override fun getRefMapperConfig() = null

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        /**
         * 限制只有不包含 .special. 的类才会被混淆
         * @see [com.github.zly2006.reden.mixin.debugger]
         */
        // todo: test
        return super.shouldApplyMixin(targetClassName, mixinClassName)// && !mixinClassName.contains(".special.")
    }

    override fun getMixins() = null
}
