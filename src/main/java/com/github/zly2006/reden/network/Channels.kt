package com.github.zly2006.reden.network

import net.minecraft.util.Identifier

val JUMP_NEXT_TICK = Identifier("reden", "jump_next_tick")
val DISABLE_BREAK_POINT = Identifier("reden", "disable_break_point")
val ROLLBACK = Identifier("reden", "rollback")
val TAG_BLOCK_POS = Identifier("reden", "tag_block_pos")
val TNT_SYNC_PACKET = Identifier("reden", "tnt_sync_packet")
val RVC_DATA_SYNC = Identifier("reden", "rvc_data_sync")
val RVC_TRACKPOINTS_C2S = Identifier("reden", "rvc_trackpoints_c2s")
fun register() {
    TagBlockPos.register()
    Rollback.register()
    ChangeBreakpointPacket.register()
    BreakPointInterrupt.register()
    StepUpdate.register()
    TntSyncPacket.register()
}
