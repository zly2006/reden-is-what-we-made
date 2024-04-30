package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.tracking.PlacementInfo
import com.github.zly2006.reden.rvc.tracking.RvcRepository
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import net.minecraft.network.NetworkSide
import net.minecraft.util.BlockRotation
import org.eclipse.jgit.api.Git
import java.io.File

class RvcManager(val side: NetworkSide) {
    val repositories = mutableMapOf<String, RvcRepository>()

    init {
        load()
    }

    fun load() {
        repositories.clear()
        File("rvc").mkdirs()
        File("rvc").listFiles()!!.asSequence()
            .filter { it.isDirectory && it.resolve(".git").exists() }
            .map { RvcRepository(Git.open(it), side = NetworkSide.CLIENTBOUND) }
            .forEach { repositories[it.name] = it }
    }

    fun getSuffixName(baseName: String): String {
        var name = baseName
        if (name in repositories) {
            var i = 2
            while (name in repositories) {
                name = "$baseName ($i)"
                i++
            }
        }
        return name
    }

    val referenceSerializer = Json {
        serializersModule = SerializersModule {
            include(serializersModule)
            include(
                serializersModuleOf(
                    TrackedStructurePart::class, TrackedPartNameSerializer()
                )
            )
        }
        prettyPrint = true
        @OptIn(ExperimentalSerializationApi::class)
        prettyPrintIndent = "  "
    }
    val dataSerializer = Json {
        serializersModule = SerializersModule {
            include(serializersModule)
            include(
                serializersModuleOf(
                    TrackedStructurePart::class, TrackedPartFullSerializer(this@RvcManager)
                )
            )
        }
    }

    inner class TrackedPartNameSerializer : KSerializer<TrackedStructurePart> {
        override val descriptor = String.serializer().descriptor
        override fun deserialize(decoder: Decoder): TrackedStructurePart {
            val name = decoder.decodeString()
            if ('/' in name) {
                val (repoName, partName) = name.split('/')
                return repositories[repoName]!!.head().regions[partName]!!
            }
            else {
                return repositories[name]!!.head().regions[""]!!
            }
        }

        override fun serialize(encoder: Encoder, value: TrackedStructurePart) {
            encoder.encodeString(value.name)
        }
    }

    class TrackedPartFullSerializer(private val manager: RvcManager) : KSerializer<TrackedStructurePart> {
        @Serializable
        class Data(
            val repoName: String,
            val partName: String,
            val relativePos: RelativeCoordinate,
            val rotation: BlockRotation,
            val tracker: StructureTracker,
            val placementInfo: PlacementInfo?
        )

        override val descriptor = Data.serializer().descriptor

        override fun deserialize(decoder: Decoder): TrackedStructurePart {
            val data = decoder.decodeSerializableValue(Data.serializer())
            val repo = manager.repositories[data.repoName]!!
            return TrackedStructurePart(
                data.partName,
                repo.head(),
                data.tracker,
            ).apply {
                placementInfo = data.placementInfo
                if (data.placementInfo?.origin != null) {
                    if (repo.head().getRelativeCoordinate(data.placementInfo.origin) != data.relativePos) {
                        // move origin
                        createPlacement(data.placementInfo.copy(origin = data.relativePos.blockPos(repo.head().origin)))
                    }
                }
            }
        }

        override fun serialize(encoder: Encoder, value: TrackedStructurePart) {
            val repoName = value.structure.name
            val partName = value.partName
            val relativePos = value.structure.getRelativeCoordinate(value.origin)
            val rotation = BlockRotation.NONE // todo
            val tracker = value.tracker
            val placementInfo = value.placementInfo
            encoder.encodeSerializableValue(
                Data.serializer(),
                Data(repoName, partName, relativePos, rotation, tracker, placementInfo)
            )
        }
    }
}
