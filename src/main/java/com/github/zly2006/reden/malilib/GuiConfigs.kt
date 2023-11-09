package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.sponsor.SponsorScreen
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.screen.Screen
import com.github.zly2006.reden.malilib.GuiConfigs.ConfigGuiTab as ConfigGuiTab1

class GuiConfigs(parent: Screen? = null): GuiConfigsBase(
    10, 50, Reden.MOD_ID, null, "reden.widget.config.title"
) {
    init {
        this.parent = parent
    }
    private var tab = ConfigGuiTab1.GENERIC
    override fun initGui() {
        super.initGui()
        val finalX = ConfigGuiTab1.values().fold(10) { x, tab ->
            val button = ButtonGeneric(x, 26, -1, 20, tab.displayName)
            button.setEnabled(tab != this.tab)
            addButton(button) { _, _ ->
                if (tab != this.tab) {
                    this.tab = tab
                    initGui()
                }
            }
            button.width + x + 2
        }
        val sponsorsButton = ButtonGeneric(finalX, 26, -1, 20, StringUtils.translate("reden.widget.config.sponsor"))
        addButton(sponsorsButton) { _, _ ->
            client!!.setScreen(SponsorScreen(this))
        }
    }
    override fun getConfigs(): MutableList<ConfigOptionWrapper> = when (tab) {
        ConfigGuiTab1.GENERIC -> ConfigOptionWrapper.createFor(GENERIC_TAB)
        ConfigGuiTab1.RVC -> ConfigOptionWrapper.createFor(RVC_TAB)
        ConfigGuiTab1.MICRO_TICK -> ConfigOptionWrapper.createFor(MICRO_TICK_TAB)
        ConfigGuiTab1.SUPER_RIGHT -> ConfigOptionWrapper.createFor(SUPER_RIGHT_TAB)
        ConfigGuiTab1.DEBUG -> ConfigOptionWrapper.createFor(DEBUG_TAB)
    }
    override fun useKeybindSearch() = true
    override fun close() {
        client!!.setScreen(parent)
    }
    enum class ConfigGuiTab(private val translationKey: String) {
        GENERIC("reden.widget.config.generic"),
        RVC("reden.widget.config.rvc"),
        MICRO_TICK("reden.widget.config.micro_tick"),
        SUPER_RIGHT("reden.widget.config.super_right"),
        DEBUG("reden.widget.config.debug"),
        ;

        val displayName: String
            get() = StringUtils.translate(translationKey)
    }
}
