package com.github.zly2006.reden.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import kotlin.reflect.full.createType

@OptIn(ExperimentalSerializationApi::class)
@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
interface PacketCodecHelper<T : CustomPayload> {
    companion object {
        val cbor = Cbor {

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
    val type = T::class.createType()
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
