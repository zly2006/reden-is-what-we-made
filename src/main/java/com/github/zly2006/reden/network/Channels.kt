package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden

val RVC_DATA_SYNC = Reden.identifier("rvc_data_sync")
fun registerChannels() {
    registerHello()
    TagBlockPos.register()
    Undo.register()
    UpdateBreakpointPacket.register()
    BreakPointInterrupt.register()
    StepOver.register()
    StepInto.register()
    Continue.register()
    TntSyncPacket.register()
    GlobalStatus.register()
    WorldStatus.register()
    HopperCDSync.register()
    StageTreeS2CPacket.register()
    registerSyncBreakpointsS2C()
}
