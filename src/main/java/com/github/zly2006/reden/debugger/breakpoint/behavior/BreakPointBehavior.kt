package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint

/**
 * 挂起
 * log
 * 统计信息
 */
abstract class BreakPointBehavior {
    abstract fun onBreakPoint(breakPoint: BreakPoint)
}
