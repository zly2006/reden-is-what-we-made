package com.github.zly2006.reden.mixin.malilib;

import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WidgetListBase.class, remap = false)
public interface IMixinWidgetListBase<TYPE, WIDGET> {
    @Accessor("lastSelectedEntry")
    TYPE lastSelectedEntry$reden();

    @Accessor("lastSelectedEntry")
    void lastSelectedEntry$reden(TYPE any);
}
