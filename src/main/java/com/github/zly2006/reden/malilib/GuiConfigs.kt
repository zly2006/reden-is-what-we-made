package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.gui.CreditScreen
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.sponsor.SponsorScreen
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.screen.Screen

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
        val creditsButton = ButtonGeneric(finalX, 26, -1, 20, "Credits")
        val sponsorsButton = ButtonGeneric(
            finalX + creditsButton.width + 2,
            26,
            -1,
            20,
            StringUtils.translate("reden.widget.config.sponsor")
        )
        addButton(creditsButton) { _, _ ->
            onFunctionUsed("credits_malilibConfigScreen")
            client!!.setScreen(CreditScreen(this))
        }
        addButton(sponsorsButton) { _, _ ->
            onFunctionUsed("buttonSponsor_malilibConfigScreen")
            client!!.setScreen(SponsorScreen(this))
        }
    }
    override fun getConfigs(): MutableList<ConfigOptionWrapper> = when (tab) {
        ConfigGuiTab.GENERIC -> ConfigOptionWrapper.createFor(GENERIC_TAB)
        ConfigGuiTab.RVC -> ConfigOptionWrapper.createFor(RVC_TAB)
        ConfigGuiTab.MICRO_TICK -> ConfigOptionWrapper.createFor(MICRO_TICK_TAB)
        ConfigGuiTab.SUPER_RIGHT -> ConfigOptionWrapper.createFor(SUPER_RIGHT_TAB)
        ConfigGuiTab.DEBUG -> ConfigOptionWrapper.createFor(DEBUG_TAB)
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
