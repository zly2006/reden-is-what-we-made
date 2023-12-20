package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import net.minecraft.util.Identifier

/**
 * 挂起
 * log
 * 统计信息
 */
abstract class BreakPointBehavior {
    var defaultPriority = 50; protected set
    abstract val id: Identifier
    abstract fun onBreakPoint(breakPoint: BreakPoint, event: Any)
}
