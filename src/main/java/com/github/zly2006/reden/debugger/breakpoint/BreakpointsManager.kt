package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import com.github.zly2006.reden.debugger.breakpoint.behavior.StatisticsBehavior
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.network.SyncBreakpointsPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.UPDATE
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import org.jetbrains.annotations.TestOnly
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry as UpdaterEntry

class BreakpointsManager(val isClient: Boolean) {
    val registry = mutableMapOf<Identifier, BreakPointType>()
    val behaviorRegistry = mutableMapOf<Identifier, BreakPointBehavior>()
    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()

    fun register(type: BreakPointType) {
        if (registry.containsKey(type.id)) error("Duplicate BreakPointType ${type.id}")
        registry[type.id] = type
    }
    fun register(behavior: BreakPointBehavior) {
        if (behaviorRegistry.containsKey(behavior.id)) error("Duplicate BreakPointBehaviorType ${behavior.id}")
        behaviorRegistry[behavior.id] = behavior
    }

    init {
        register(BlockUpdateOtherBreakpoint)
        register(BlockUpdatedBreakpoint)
        register(RedstoneMeterBreakpoint)

        register(FreezeGame())
        register(StatisticsBehavior())
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun read(buf: PacketByteBuf): BreakPoint {
        return Cbor.decodeFromByteArray(breakpointSerializer(), buf.readByteArray())
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun write(buf: PacketByteBuf, bp: BreakPoint) {
        buf.writeByteArray(Cbor.encodeToByteArray(breakpointSerializer(), bp))
    }

    fun sendAll(sender: PacketSender) {
        sender.sendPacket(SyncBreakpointsPacket(breakpointMap.values.toList()))
    }

    fun clear() {
        breakpointMap.clear()
    }

    fun <T : UpdaterEntry> checkBreakpointsForUpdating(stage: AbstractBlockUpdateStage<T>) {
        breakpointMap.values.asSequence()
            .filter { it.flags and ENABLED != 0 }
            .filter { stage.world == it.serverWorld }
            .forEach { it.call(stage) }
    }

    fun checkBreakpointsForScheduledTick() {

    }

    fun sync(breakpoint: BreakPoint) {
        if (isClient) {
            ClientPlayNetworking.send(UpdateBreakpointPacket(
                breakpoint,
                flag = UPDATE or breakpoint.flags,
                bpId = breakpoint.id
            ))
        } else {
            server.sendToAll(UpdateBreakpointPacket(
                breakpoint,
                flag = UPDATE or breakpoint.flags,
                bpId = breakpoint.id
            ))
        }
    }

    fun syncFlags(breakpoint: BreakPoint) {
        if (isClient) {
            ClientPlayNetworking.send(UpdateBreakpointPacket(
                null,
                flag = breakpoint.flags,
                bpId = breakpoint.id
            ))
        } else {
            server.sendToAll(UpdateBreakpointPacket(
                null,
                flag = breakpoint.flags,
                bpId = breakpoint.id
            ))
        }
    }

    fun save(path: Path) {
        require(!isClient) {
            "Cannot save breakpoint on client side"
        }
        Reden.LOGGER.info("Saving breakpoints to $path")
        val breakpoints = breakpointMap.values.toList()
        path.writeText(json.encodeToString(ListSerializer(breakpointSerializer()), breakpoints))
    }

    fun load(path: Path) {
        require(!isClient) {
            "Cannot load breakpoint on client side"
        }
        if (!path.exists()) return
        Reden.LOGGER.info("Loading breakpoints from $path")
        val breakpoints = json.decodeFromString(ListSerializer(breakpointSerializer()), path.readText())
        breakpointMap.clear()
        breakpoints.forEach {
            breakpointMap[it.id] = it
        }
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun breakpointSerializer() = object : KSerializer<BreakPoint> {
        override val descriptor: SerialDescriptor by lazy(LazyThreadSafetyMode.PUBLICATION) {
            buildSerialDescriptor("redenmc.breakpoint", PolymorphicKind.OPEN) {
                element("type", String.serializer().descriptor)
                element(
                    "value",
                    buildSerialDescriptor("redenmc.breakpoint.serialization", SerialKind.CONTEXTUAL)
                )
            }
        }

        override fun serialize(encoder: Encoder, value: BreakPoint) {
            @Suppress("UNCHECKED_CAST")
            val actualSerializer = value.type.kSerializer() as KSerializer<BreakPoint>
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, value.type.id.toString())
                encodeSerializableElement(descriptor, 1, actualSerializer, value)
            }
        }

        override fun deserialize(decoder: Decoder): BreakPoint = decoder.decodeStructure(descriptor) {
            var identifier: String? = null
            var value: BreakPoint? = null
            if (decodeSequentially()) {
                return@decodeStructure decodeSequentially(this)
            }

            mainLoop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@mainLoop
                    0 -> identifier = decodeStringElement(descriptor, index)

                    1 -> {
                        identifier = requireNotNull(identifier) { "Cannot read breakpoint before its type token" }
                        val serializer = findActualSerializer(identifier)
                        value = decodeSerializableElement(descriptor, index, serializer)
                    }
                    else -> throw SerializationException(
                        "Invalid index in breakpoint deserialization of " +
                                (identifier ?: "unknown id") +
                                "\n Expected 0, 1 or DECODE_DONE(-1), but found $index"
                    )
                }
            }
            requireNotNull(value) {
                "No breakpoint data read for type $identifier"
            }
        }

        private fun decodeSequentially(compositeDecoder: CompositeDecoder): BreakPoint {
            val serializer = findActualSerializer(compositeDecoder.decodeStringElement(descriptor, 0))
            return compositeDecoder.decodeSerializableElement(descriptor, 1, serializer)
        }

        fun findActualSerializer(
            identifier: String?
        ) = registry[Identifier(identifier)]?.kSerializer()
            ?: throw SerializationException("breakpoint type $identifier not found")
    }

    companion object {
        @TestOnly
        var testBreakpointManager: BreakpointsManager? = null

        /**
         * Get a breakpoint manager for serialization.
         *
         * You should **NEVER** get breakpoints from its return value.
         * This returned breakpoint manager has no warranty to be client/server side or synced.
         */
        @Deprecated("Use fun breakpointSerializer()", level = DeprecationLevel.WARNING)
        fun getBreakpointManager() =
            if (testBreakpointManager != null && FabricLoader.getInstance().isDevelopmentEnvironment) {
                testBreakpointManager!!
            } else if (isClient) {
                MinecraftClient.getInstance().data.breakpoints
            } else {
                server.data.breakpoints
            }

    }
}

@Suppress("DEPRECATION")
fun breakpointSerializer() = BreakpointsManager.getBreakpointManager().breakpointSerializer()