package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.breakpoint.IdentifierSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

// Same as IdentifierSerializer
private class Serializer : KSerializer<BreakPointBehavior> {
    override val descriptor: SerialDescriptor = IdentifierSerializer.descriptor
    override fun deserialize(decoder: Decoder): BreakPointBehavior {
        val id = IdentifierSerializer.deserialize(decoder)
        return BreakpointsManager.getBreakpointManager().behaviorRegistry[id] ?: error("Unknown behavior id: $id")
    }

    override fun serialize(encoder: Encoder, value: BreakPointBehavior) {
        IdentifierSerializer.serialize(encoder, value.id)
    }
}
@Serializable(with = Serializer::class)
abstract class BreakPointBehavior {
    var defaultPriority = 50; protected set
    abstract val id: Identifier
    
    abstract fun onBreakPoint(breakPoint: BreakPoint, event: Any)
}
