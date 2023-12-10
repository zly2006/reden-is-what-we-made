package com.github.zly2006.reden.access;

import com.github.zly2006.reden.debugger.TickStage;
import org.jetbrains.annotations.Nullable;

public interface TickStageOwnerAccess {
    @Nullable
    TickStage getTickStage$reden();
    void setTickStage$reden(@Nullable TickStage tickStage);
    boolean getTicked$reden();
    void setTicked$reden(boolean ticked);
}
