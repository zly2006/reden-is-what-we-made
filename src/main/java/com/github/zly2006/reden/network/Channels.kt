package com.github.zly2006.reden.network

import net.minecraft.util.Identifier

val JUMP_NEXT_TICK = Identifier("reden", "jump_next_tick")
val DISABLE_BREAK_POINT = Identifier("reden", "disable_break_point")
val ROLLBACK = Identifier("reden", "rollback")
val TAG_BLOCK_POS = Identifier("reden", "tag_block_pos")

fun register() {
    TagBlockPos.register()
    Rollback.register()
    ChangeBreakpointPacket.register()
    BreakPointInterrupt.register()
}
