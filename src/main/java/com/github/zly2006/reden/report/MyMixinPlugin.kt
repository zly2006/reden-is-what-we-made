package com.github.zly2006.reden.report

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin

class MyMixinPlugin: RestrictiveMixinConfigPlugin() {
    override fun getRefMapperConfig() = null

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
        runBlocking {
            yield()
            this
        }
    }

    override fun getMixins() = null
}
