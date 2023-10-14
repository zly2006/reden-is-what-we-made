package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden

val JUMP_NEXT_TICK = Reden.identifier("jump_next_tick")
val DISABLE_BREAK_POINT = Reden.identifier("disable_break_point")
val ROLLBACK = Reden.identifier("rollback")
val TAG_BLOCK_POS = Reden.identifier("tag_block_pos")
val TNT_SYNC_PACKET = Reden.identifier("tnt_sync_packet")
val RVC_DATA_SYNC = Reden.identifier("rvc_data_sync")
val RVC_TRACKPOINTS_C2S = Reden.identifier("rvc_trackpoints_c2s")
fun register() {
    TagBlockPos.register()
    Rollback.register()
    ChangeBreakpointPacket.register()
    BreakPointInterrupt.register()
    StepUpdate.register()
    TntSyncPacket.register()
    RvcTrackpointsC2SRequest.register()
    RvcDataS2CPacket.register()
    HopperCDSync.register()
}
