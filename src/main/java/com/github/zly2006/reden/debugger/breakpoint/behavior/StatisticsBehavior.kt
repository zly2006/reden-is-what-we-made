package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import net.minecraft.util.Identifier

class StatisticsBehavior: BreakPointBehavior() {
    override val id: Identifier
        get() = Reden.identifier("statistics_behavior")

    override fun onBreakPoint(breakPoint: BreakPoint, event: Any) {
    }
}
