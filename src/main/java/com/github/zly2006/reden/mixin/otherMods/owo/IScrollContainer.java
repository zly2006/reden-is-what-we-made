package com.github.zly2006.reden.mixin.otherMods.owo;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ScrollContainer.class, remap = false)
public interface IScrollContainer {
    @Accessor("scrollOffset")
    double getScrollOffset();
    @Accessor("scrollOffset")
    void setScrollOffset(double scrollOffset);
    @Accessor("currentScrollPosition")
    double getCurrentScrollPosition();
    @Accessor("currentScrollPosition")
    void setCurrentScrollPosition(double currentScrollPosition);
}
