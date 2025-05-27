package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.screens.Screen

class GuiConfigs(parent: Screen? = null): GuiConfigsBase(
    10, 50, Reden.MOD_ID, null, "reden.widget.config.title"
) {
    init {
        this.parent = parent
    }

    private var tab = ConfigGuiTab.GENERIC
    override fun initGui() {
        super.initGui()
        val finalX = ConfigGuiTab.entries.fold(10) { x, tab ->
            val button = ButtonGeneric(x, 26, -1, 20, tab.displayName)
            button.setEnabled(tab != this.tab)
            addButton(button) { _, _ ->
                if (tab != this.tab) {
                    this.tab = tab
                    listWidget?.resetScrollbarPosition()
                    initGui()
                }
            }
            button.width + x + 2
        }
    }
    override fun getConfigs(): MutableList<ConfigOptionWrapper> = when (tab) {
        ConfigGuiTab.GENERIC -> ConfigOptionWrapper.createFor(GENERIC_TAB)
        ConfigGuiTab.DEBUG -> ConfigOptionWrapper.createFor(DEBUG_TAB)
    }
    override fun useKeybindSearch() = true

    override fun onClose() {
        mc.setScreen(parent)
    }

    enum class ConfigGuiTab(private val translationKey: String) {
        GENERIC("reden.widget.config.generic"),
        DEBUG("reden.widget.config.debug"),
        ;

        val displayName: String
            get() = StringUtils.translate(translationKey)
    }
}
