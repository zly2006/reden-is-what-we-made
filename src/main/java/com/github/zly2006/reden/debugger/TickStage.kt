package com.github.zly2006.reden.debugger

import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.MutableText
import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage?,
    open val displayName: MutableText,
    open val description: MutableText = Text.empty()
) {
    constructor(name: String, parent: TickStage?): this(
        name, parent,
        Text.translatable("reden.debugger.tick_stage.$name"),
        Text.translatable("reden.debugger.tick_stage.$name.desc")
    )
    companion object {
        private var id = 0
    }
    var id = TickStage.id++
    private var debugExpectedChildrenSize = -1
    val children = mutableListOf<TickStage>()
    enum class DisplayLevel {
        ALWAYS_HIDE, HIDE, ALWAYS_FOLD, FULL
    }
    var displayLevel: DisplayLevel = DisplayLevel.FULL
    enum class StageStatus {
        Initialized, Pending, Ticked, Finished
    }
    var status = StageStatus.Initialized // todo

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
