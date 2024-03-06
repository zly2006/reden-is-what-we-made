package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair

val TAG_BLOCK_POS = Reden.identifier("tag_block_pos")
val TNT_SYNC_PACKET = Reden.identifier("tnt_sync_packet")
val RVC_DATA_SYNC = Reden.identifier("rvc_data_sync")
fun register() {

    KeyPair.genKeyPair(JSch(), KeyPair.RSA, 2048).apply {
        writePrivateKey("A")
        writePublicKey("A.pub", "BB")
    }
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
    WorldStatus.register()
    RvcTrackpointsC2SRequest.register()
    RvcDataS2CPacket.register()
    HopperCDSync.register()
    StageTreeS2CPacket.register()
    SyncBreakpointsPacket.register()
}
