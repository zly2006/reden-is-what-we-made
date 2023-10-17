package com.github.zly2006.reden.report

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin

class MyMixinPlugin: RestrictiveMixinConfigPlugin() {
    override fun getRefMapperConfig() = null

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun getMixins() = null
}
