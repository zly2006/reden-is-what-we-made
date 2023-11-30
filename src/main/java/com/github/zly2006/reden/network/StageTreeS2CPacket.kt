package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.gui.DebuggerComponent
import com.github.zly2006.reden.debugger.tree.StageIo
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import io.wispforest.owo.ui.hud.Hud
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

class StageTreeS2CPacket(val tree: StageTree) : FabricPacket {
    companion object {
        val id = Reden.identifier("stage_tree_s2c")
        val pType = PacketType.create(id) {
            StageTreeS2CPacket(StageIo.readStageTree(it))
        }!!

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                    val mc = MinecraftClient.getInstance()
                    player.sendMessage("Tick stage tree")
                    var node = packet.tree.child
                    while (node != null) {
                        player.sendMessage(node.stage.displayName, false)
                        node = node.parent
                    }
                    Hud.remove(Reden.identifier("debugger"))
                    Hud.add(Reden.identifier("debugger")) {
                        DebuggerComponent(packet.tree).asHud()
                    }
                }
            }
        }
    }
    override fun write(buf: PacketByteBuf) {
        StageIo.writeStageTree(tree, buf)
    }

    override fun getType() = pType
}