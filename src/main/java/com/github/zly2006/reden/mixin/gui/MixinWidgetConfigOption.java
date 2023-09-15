package com.github.zly2006.reden.mixin.gui;

import com.github.zly2006.reden.malilib.gui.button.ConfigButtonCommandHotkeyList;
import com.github.zly2006.reden.malilib.options.RedenConfigCommandHotkeyList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetConfigOption.class, remap = false)
public abstract class MixinWidgetConfigOption {
    @Accessor
    protected abstract IKeybindConfigGui getHost();

    @Shadow
    protected abstract void addConfigButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton);

    @Inject(method = "addConfigOption", at = @At("TAIL"))
    private void addConfigOptionForCommandHotkeyList(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci) {
        if (config instanceof RedenConfigCommandHotkeyList commandHotkeyList) {
            int configHeight = 20;
            ConfigButtonCommandHotkeyList optionButton = new ConfigButtonCommandHotkeyList(x, y, configWidth, configHeight, commandHotkeyList, this.getHost(), this.getHost().getDialogHandler());
            this.addConfigButtonEntry(x + configWidth + 2, y, (IConfigResettable) config, optionButton);
        }
    }
}
