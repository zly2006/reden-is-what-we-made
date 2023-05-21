package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint

abstract class BreakPointBehavior {
    abstract fun onBreakPoint(breakPoint: BreakPoint)
}
