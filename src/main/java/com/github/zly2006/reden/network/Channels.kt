package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden

val TAG_BLOCK_POS = Reden.identifier("tag_block_pos")
val TNT_SYNC_PACKET = Reden.identifier("tnt_sync_packet")
val RVC_DATA_SYNC = Reden.identifier("rvc_data_sync")
fun register() {
    Hello.register()
    TagBlockPos.register()
    Undo.register()
    UpdateBreakpointPacket.register()
    BreakPointInterrupt.register()
    StepOver.register()
    StepInto.register()
    Continue.register()
    TntSyncPacket.register()
    GlobalStatus.register()
    RvcTrackpointsC2SRequest.register()
    RvcDataS2CPacket.register()
    HopperCDSync.register()
    StageTreeS2CPacket.register()
    SyncBreakpointsPacket.register()
}
