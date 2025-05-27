package com.github.zly2006.reden.network

import com.github.zly2006.reden.utils.codec.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.typeOf

@OptIn(ExperimentalSerializationApi::class)
@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
interface PacketCodecHelper<T : CustomPacketPayload> {
    companion object {
        val cbor = Cbor {
            serializersModule = SerializersModule {
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

    val ID: CustomPacketPayload.Type<T>
    val CODEC: StreamCodec<FriendlyByteBuf, T>
    fun playC2S() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Suppress("FunctionName")
inline fun <reified T : CustomPacketPayload> PacketCodec(id: ResourceLocation): PacketCodecHelper<T> {
    val type = typeOf<T>()
    return object : PacketCodecHelper<T> {
        override val ID = CustomPacketPayload.Type<T>(id)
        override val CODEC = StreamCodec.of<FriendlyByteBuf, T>({ buf, obj ->
            buf.writeByteArray(PacketCodecHelper.cbor.encodeToByteArray(serializer(type), obj))
        }, { buf ->
            val bytes = buf.readByteArray()
            PacketCodecHelper.cbor.decodeFromByteArray(serializer(type), bytes) as T
        })
    }
}
