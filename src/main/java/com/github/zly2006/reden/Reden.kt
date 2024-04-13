package com.github.zly2006.reden

import carpet.CarpetExtension
import carpet.CarpetServer
import com.github.zly2006.reden.access.BlockEntityInterface
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.behalf.registerBehalf
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.indexing.blockId
import com.github.zly2006.reden.indexing.entityId
import com.github.zly2006.reden.indexing.propertyId
import com.github.zly2006.reden.network.registerChannels
import com.github.zly2006.reden.rvc.registerRvc
import com.github.zly2006.reden.transformers.ThisIsReden
import com.github.zly2006.reden.utils.ResourceLoader
import com.github.zly2006.reden.utils.ResourceLoader.loadLang
import com.github.zly2006.reden.utils.TaskScheduler
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.mojang.blaze3d.platform.GlDebugInfo
import com.redenmc.bragadier.ktdsl.register
import com.redenmc.bragadier.ktdsl.then
import fi.dy.masa.litematica.render.LitematicaRenderer
import fi.dy.masa.litematica.world.SchematicWorldHandler
import kotlinx.coroutines.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.SharedConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.BlockStateArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.entity.EntityStatuses
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


class Reden : ModInitializer, CarpetExtension {
    companion object {
        // @formatter:off
        const val MOD_ID = "reden"
        const val MOD_NAME = "Reden"
        const val CONFIG_FILE = "reden/config.json"
        private val MOD_METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata
        const val REDEN_HIGHEST_MIXIN_PRIORITY = 10
        @JvmField val MOD_VERSION: Version = MOD_METADATA.version
        @JvmField val BUILD_TIME = Date(MOD_METADATA.getCustomValue("reden").asObject["build_timestamp"].asString.toLong())
        @JvmField val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)
        @JvmField val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
        @JvmField val LOGO = identifier("reden_16.png")
        @JvmStatic val isRedenDev = System.getProperty("reden.debug", FabricLoader.getInstance().isDevelopmentEnvironment.toString()).toBoolean()
        // @formatter:on

        @JvmStatic
        @Contract("_ -> new")
        fun identifier(id: String): Identifier {
            return Identifier(MOD_ID, id)
        }
    }

    private var serverStartTime: Long = 0

    override fun version(): String {
        return "reden"
    }

    override fun onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(RedenCarpetSettings.Options::class.java)
    }

    override fun canHasTranslations(lang: String): Map<String, String> {
        return loadLang(lang)!!
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it
            serverStartTime = System.currentTimeMillis()
        }
        registerChannels()
        CarpetServer.manageExtension(this)
        CommandRegistrationCallback.EVENT.register { dispatcher, access, _ ->
            registerBehalf(dispatcher)
            dispatcher.register {
                literal("reden") {
                    literal("version").executes { context ->
                        context.source.sendMessage(Text.of("Reden v" + MOD_VERSION.friendlyString))
                        context.source.sendMessage(Text.of("Build time: $BUILD_TIME"))
                        1
                    }
                }
                literal("neofetch").executes { context ->
                    val ja = Gson().fromJson(
                        ResourceLoader.loadString("assets/reden/neofetch.json"),
                        JsonArray::class.java
                    )
                    fun color(s: String) = s.map {
                        Text.literal("█").formatted(Formatting.byCode(it))
                    }.fold(Text.empty()) { acc, text -> acc.append(text) }

                    val lineSeq = buildList<Text> {
                        // @formatter:off
                        add(Text.literal(context.source.name))
                        add(Text.literal("-".repeat(context.source.name.length)))
                        add(Text.literal("Minecraft ${SharedConstants.getGameVersion().name} (${server.serverModName})"))
                        add(Text.literal("Reden $MOD_VERSION"))
                        add(Text.literal("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}"))
                        add(Text.literal("Java: " + System.getProperty("java.vm.name")
                            .replace("vm", "", true)
                            .replace("server", "", true)
                            .trim() + " " + System.getProperty("java.vm.version")))
                        add(Text.literal("MC Memory: ${Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() / 1024 / 1024}/${Runtime.getRuntime().maxMemory() / 1024 / 1024} MiB"))
                        add(Text.literal("Server Uptime: ${(System.currentTimeMillis() - serverStartTime).milliseconds}"))
                        // @formatter:on
                        if (isClient) {
                            val mc = MinecraftClient.getInstance()
                            runBlocking(mc.asCoroutineDispatcher()) {
                                buildList {
                                    add(Text.literal("CPU: ${GlDebugInfo.getCpuInfo()}"))
                                    add(Text.literal("Display: ${mc.window.framebufferWidth}x${mc.window.framebufferHeight} (${GlDebugInfo.getVendor()})"))
                                    add(Text.literal("Driver: ${GlDebugInfo.getRenderer()}"))
                                    add(Text.literal("OpenGL: ${GlDebugInfo.getVersion()}"))
                                }
                            }.let { addAll(it) }
                        }
                        add(Text.empty())
                        add(color("01234567"))
                        add(color("89abcdef"))
                    }.iterator()
                    var textLength = 0
                    ja.asSequence().forEach {
                        val text = Text.empty()
                        for (t in it.asJsonArray) {
                            text.append(Text.Serialization.fromJsonTree(t))
                        }
                        if (lineSeq.hasNext()) {
                            text.append("  ").apply { }
                            textLength = text.string.length
                            text.append(lineSeq.next())
                        }
                        context.source.sendMessage(text)
                    }
                    lineSeq.forEachRemaining {
                        context.source.sendMessage(Text.literal(" ".repeat(textLength)).append(it))
                    }
                    1
                }
            }
            // Debug command
            if (isRedenDev) {
                dispatcher.register {
                    literal("reden-debug") {
                        literal("top-undo").executes { context ->
                            context.source.player!!.data().topUndo()
                            1
                        }
                        literal("top-redo").executes { context ->
                            context.source.player!!.data().topRedo()
                            1
                        }
                        literal("schematic").then { // Note: single-player mode only
                            literal("setblock") {
                                argument("pos", BlockPosArgumentType.blockPos()).then {
                                    argument("block", BlockStateArgumentType.blockState(access)).executes { context ->
                                        val client = MinecraftClient.getInstance()
                                        assert(client.player != null)
                                        val pos = BlockPosArgumentType.getBlockPos(context, "pos")
                                        SchematicWorldHandler.getSchematicWorld()!!.setBlockState(
                                            pos, BlockStateArgumentType.getBlockState(
                                                context, "block"
                                            ).blockState, 3
                                            )
                                        SchematicWorldHandler.getSchematicWorld()!!
                                            .scheduleChunkRenders(pos.x shr 4, pos.z shr 4)
                                        LitematicaRenderer.getInstance().worldRenderer.markNeedsUpdate()
                                        client.player!!.sendMessage(
                                            SchematicWorldHandler.getSchematicWorld()!!.getBlockState(pos).block.name
                                        )
                                        1
                                    }
                                }
                            }
                        }
                        literal("last-saved-nbt").then {
                            argument("pos", BlockPosArgumentType.blockPos()).executes { context ->
                                val pos = BlockPosArgumentType.getBlockPos(context, "pos")
                                val blockEntity = context.source.world.getBlockEntity(pos)
                                if (blockEntity == null) {
                                    context.source.sendError(Text.of("No block entity at " + pos.toShortString()))
                                    return@executes 0
                                }
                                val lastSavedNbt = (blockEntity as BlockEntityInterface).lastSavedNbt
                                if (lastSavedNbt == null) {
                                    context.source.sendError(Text.of("No last saved NBT at " + pos.toShortString()))
                                    return@executes 0
                                }
                                context.source.sendMessage(Text.of(lastSavedNbt.toString()))
                                1
                            }
                        }
                        literal("shadow-item").then {
                            argument("item", ItemStackArgumentType.itemStack(access)).executes { context ->
                                val itemStackArgument = ItemStackArgumentType.getItemStackArgument(context, "item")
                                val stack = itemStackArgument.createStack(1, true)
                                val inventory = context.source.player!!.inventory
                                for (i in 0..1) {
                                    val emptySlot = inventory.emptySlot
                                    inventory.setStack(emptySlot, stack)
                                }
                                context.source.player!!.currentScreenHandler.syncState()
                                1
                            }
                        }
                        literal("totem-of-undying").executes { context ->
                            val player = context.source.player
                            player?.networkHandler?.sendPacket(
                                EntityStatusS2CPacket(
                                    player, EntityStatuses.USE_TOTEM_OF_UNDYING
                                )
                            )
                            1
                        }
                        literal("delay-test").executes {
                            Thread.sleep((35 * 1000).toLong())
                            it.source.sendMessage(Text.of("35 seconds passed"))
                            1
                        }
                    }
                }
            }
            registerRvc(dispatcher)
            if (dispatcher !is ThisIsReden) {
                throw RuntimeException("This is not Reden!")
            }
            else {
                LOGGER.info("This is Reden!")
            }
        }
        ServerTickEvents.END_SERVER_TICK.register(TaskScheduler)

        GlobalScope.launch(Dispatchers.IO) {
            LOGGER.info("Loading indexes...")
            try {
                entityId
                blockId
                propertyId
            } catch (e: Exception) {
                LOGGER.error("Loading indexes.", e)
            }
        }
    }
}
