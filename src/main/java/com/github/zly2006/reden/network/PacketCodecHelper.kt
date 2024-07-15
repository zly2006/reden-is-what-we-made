package com.github.zly2006.reden.network

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.utils.codec.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import kotlin.reflect.typeOf

@OptIn(ExperimentalSerializationApi::class)
@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
interface PacketCodecHelper<T : CustomPayload> {
    companion object {
        val cbor = Cbor {
            serializersModule = SerializersModule {
                include(serializersModuleOf(BreakpointsManager.Companion.Serializer))
                include(serializersModuleOf(UUIDSerializer))
                include(serializersModuleOf(BlockPosSerializer))
                include(serializersModuleOf(IdentifierSerializer))
                include(serializersModuleOf(Vec3dSerializer))
                include(serializersModuleOf(NbtSerializer))
                include(serializersModuleOf(TextSerializer))
                include(serializersModuleOf(FabricVersionSerializer))
            }
        }
    }

    val ID: CustomPayload.Id<T>
    val CODEC: PacketCodec<PacketByteBuf, T>
    fun playC2S() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Suppress("FunctionName")
inline fun <reified T : CustomPayload> PacketCodec(id: Identifier): PacketCodecHelper<T> {
    val type = typeOf<T>()
    return object : PacketCodecHelper<T> {
        override val ID = CustomPayload.Id<T>(id)
        override val CODEC = PacketCodec.ofStatic<PacketByteBuf, T>({ buf, obj ->
            buf.writeByteArray(PacketCodecHelper.cbor.encodeToByteArray(serializer(type), obj))
        }, { buf ->
            val bytes = buf.readByteArray()
            PacketCodecHelper.cbor.decodeFromByteArray(serializer(type), bytes) as T
        })
    }
}
