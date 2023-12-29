package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.utils.server
import io.wispforest.owo.ui.container.FlowLayout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

interface BreakPointType {
    val id: Identifier
    val description: Text
    fun create(id: Int): BreakPoint
    fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {

    }
    fun kSerializer(): KSerializer<out BreakPoint>
}

interface BreakPoint {
    val id: Int
    @Transient // Note
    val type: BreakPointType
    @Serializable
    data class Handler(
        val type: BreakPointBehavior,
        var priority: Int = type.defaultPriority,
        var name: String
    )
    var name: String
    /**
     * Note: in this abstract class we only store [world].
     * We will neither **check pos** nor **serialize it** in this class, check it in subclasses [call] method
     */
    val pos: BlockPos?
    fun setPosition(pos: BlockPos): Unit = throw UnsupportedOperationException()
    @Serializable(with = IdentifierSerializer::class)
    var world: Identifier?
    val serverWorld: ServerWorld?
        get() = world?.let { server.getWorld(RegistryKey.of(RegistryKeys.WORLD, it)) }

    /**
     * We only use the lower 16 bits, feel free to use other in the subclasses
     *
     * @see com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion
     */
    var flags: Int
    val handler: MutableList<Handler>
    fun call(event: Any) {
        handler.sortBy { it.priority }
        handler.forEach { it.type.onBreakPoint(this, event) }
    }
}

object IdentifierSerializer: KSerializer<Identifier> {
    override val descriptor = String.serializer().descriptor
    override fun deserialize(decoder: Decoder) = Identifier(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Identifier) { encoder.encodeString(value.toString()) }
}

object BlockPosSerializer: KSerializer<BlockPos> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = listSerialDescriptor<Int>()
    override fun deserialize(decoder: Decoder): BlockPos {
        val list = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return BlockPos(list[0], list[1], list[2])
    }
    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), listOf(value.x, value.y, value.z))
    }
}
