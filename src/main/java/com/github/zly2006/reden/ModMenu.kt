package com.github.zly2006.reden

import com.github.zly2006.reden.malilib.GuiConfigs
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class ModMenu: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(::GuiConfigs)
}
