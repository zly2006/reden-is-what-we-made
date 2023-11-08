package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint

/**
 * 挂起
 * log
 * 统计信息
 */
abstract class BreakPointBehavior {
    val priority = 0
    abstract fun onBreakPoint(breakPoint: BreakPoint, event: Any)
}
