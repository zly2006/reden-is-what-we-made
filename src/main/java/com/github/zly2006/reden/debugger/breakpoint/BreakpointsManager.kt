package com.github.zly2006.reden.debugger.breakpoint

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
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.level.storage.LevelStorage
import org.jetbrains.annotations.TestOnly
import kotlin.io.path.*
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry as UpdaterEntry

class BreakpointsManager(val isClient: Boolean) {
    val registry = mutableMapOf<Identifier, BreakPointType>()
    val behaviorRegistry = mutableMapOf<Identifier, BreakPointBehavior>()
    private var currentBpId = 0
    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun register(type: BreakPointType) {
        if (registry.containsKey(type.id)) error("Duplicate BreakPointType ${type.id}")
        registry[type.id] = type
    }

    fun register(behavior: BreakPointBehavior) {
        if (behaviorRegistry.containsKey(behavior.id)) error("Duplicate BreakPointBehaviorType ${behavior.id}")
        behaviorRegistry[behavior.id] = behavior
    }

    fun saveBreakpointState(
        session: LevelStorage.Session
    ) {
        val saveStateFile = session.directory.path / "redenBreakpoints.json"
        saveStateFile.apply {
            deleteIfExists()
            createFile()
            println("SAVE DATA")
            if (breakpointMap.isEmpty()) {
                println("im empty")
            }
            println(breakpointMap.values.toList())
            writeText(json.encodeToString(ListSerializer(breakpointSerializer()), breakpointMap.values.toList()))
        }
    }

    fun loadBreakpointState(
        session: LevelStorage.Session
    ) {
        val saveStateFile = session.directory.path / "redenBreakpoints.json"
        saveStateFile.run {
            if (notExists()) return
            if (fileSize() == 0L) {
                deleteIfExists()
                return
            }
            breakpointMap.clear()
            json.decodeFromString(ListSerializer(breakpointSerializer()), this.readText())
        }.forEach {
            breakpointMap[it.id] = it
        }
    }

    init {
        register(BlockUpdateOtherBreakpoint)
        register(BlockUpdatedBreakpoint)
        register(RedstoneMeterBreakpoint)

        register(FreezeGame())
        register(StatisticsBehavior())

        // todo: debug only
        if (!isClient) {
            breakpointMap[0] = BlockUpdateOtherBreakpoint.create(0).apply {
                pos = BlockPos.ORIGIN
                options = BlockUpdateEvent.NC
                world = World.OVERWORLD.value
                name = "Test"
                handler.add(BreakPoint.Handler(FreezeGame(), name = "Test Handler"))
            }
            currentBpId++
        }
    }

    fun read(buf: PacketByteBuf): BreakPoint {
        val id = buf.readIdentifier()
        val bpId = buf.readVarInt()
        return registry[id]?.create(bpId)?.apply {
            name = buf.readString()
            world = buf.readIdentifier()
            val handlerSize = buf.readVarInt()
            repeat(handlerSize) {
                handler.add(
                    BreakPoint.Handler(
                        behaviorRegistry[buf.readIdentifier()] ?: error("Unknown behavior type: $id"),
                        buf.readVarInt(),
                        buf.readString()
                    )
                )
            }
            read(buf)
        } ?: throw Exception("Unknown BreakPoint $id")
    }

    fun write(buf: PacketByteBuf, bp: BreakPoint) {
        buf.writeIdentifier(bp.type.id)
        buf.writeVarInt(bp.id)
        buf.writeString(bp.name)
        buf.writeIdentifier(bp.world!!)
        buf.writeVarInt(bp.handler.size)
        bp.handler.forEach {
            buf.writeIdentifier(it.type.id)
            buf.writeVarInt(it.priority)
            buf.writeString(it.name)
        }
        bp.write(buf)
    }

    fun sendAll(sender: PacketSender) {
        sender.sendPacket(SyncBreakpointsPacket(breakpointMap.values))
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
            ClientPlayNetworking.send(
                UpdateBreakpointPacket(
                    breakpoint,
                    flag = UPDATE or breakpoint.flags,
                    bpId = breakpoint.id
                )
            )
        } else {
            server.sendToAll(
                UpdateBreakpointPacket(
                    breakpoint,
                    flag = UPDATE or breakpoint.flags,
                    bpId = breakpoint.id
                )
            )
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
                    CompositeDecoder.DECODE_DONE -> {
                        break@mainLoop
                    }

                    0 -> {
                        identifier = decodeStringElement(descriptor, index)
                    }

                    1 -> {
                        identifier =
                            requireNotNull(identifier) { "Cannot read polymorphic value before its type token" }
                        val serializer = findActualSerializer(identifier)
                        value = decodeSerializableElement(descriptor, index, serializer)
                    }

                    else -> throw SerializationException(
                        "Invalid index in polymorphic deserialization of " +
                                (identifier ?: "unknown class") +
                                "\n Expected 0, 1 or DECODE_DONE(-1), but found $index"
                    )
                }
            }
            requireNotNull(value) { "Polymorphic value has not been read for class $identifier" } as BreakPoint
        }

        private fun decodeSequentially(compositeDecoder: CompositeDecoder): BreakPoint {
            val serializer = findActualSerializer(compositeDecoder.decodeStringElement(descriptor, 0))
            return compositeDecoder.decodeSerializableElement(descriptor, 1, serializer)
        }


        fun findActualSerializer(
            identifier: String?
        ): KSerializer<out BreakPoint> =
            registry[Identifier(identifier)]?.kSerializer() ?: error("")
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
