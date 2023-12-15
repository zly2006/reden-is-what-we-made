package com.github.zly2006.reden.debugger

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage?,
) {
    companion object {
        private var id = 0
    }
    val id = TickStage.id++
    // val createdAt = Thread.getAllStackTraces()[Thread.currentThread()]
    private var debugExpectedChildrenSize = -1
    init {
        /*
        val nameRegex = Regex("[\\w\\-]+")
        require(nameRegex.matches(name)) { "Invalid tick stage name: $name" }
         */
    }
    open val displayName = Text.translatable("reden.debugger.tick_stage.$name")
    val description = Text.translatable("reden.debugger.tick_stage.$name.desc")
    val children = mutableListOf<TickStage>()

    open fun writeByteBuf(buf: PacketByteBuf) {
    }

    open fun readByteBuf(buf: PacketByteBuf) {
    }

    open fun reset(): Unit = throw UnsupportedOperationException("Reset not supported for tick stage $name")

    open fun postTick() {
    }

    open fun preTick() {

    }

    open fun focused(mc: MinecraftClient) {
        assert(mc.isOnThread) { "Focused on wrong thread" }
    }

    open fun unfocused(mc: MinecraftClient) {
        assert(mc.isOnThread) { "Focused on wrong thread" }
    }
}
