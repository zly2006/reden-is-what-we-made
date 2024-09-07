package com.github.zly2006.reden.mixin.malilib;

import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiListBase.class, remap = false)
public interface IMixinGuiListBase<TYPE, WIDGET extends WidgetListEntryBase<TYPE>, WIDGETLIST extends WidgetListBase<TYPE, WIDGET>> {
    @Accessor("widget")
    WIDGETLIST widget$reden();
}
