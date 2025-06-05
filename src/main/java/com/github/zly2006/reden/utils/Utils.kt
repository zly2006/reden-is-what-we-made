package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.exceptions.RedenException
import com.github.zly2006.reden.utils.multiver.Text
import com.google.gson.Gson
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.discovery.ModResolutionException
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.lighting.LightEngine
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.math.max
import kotlin.math.min

lateinit var server: MinecraftServer

fun Position.toBlockPos(): BlockPos {
    return BlockPos.containing(this)
}

fun Player.sendMessage(s: String) {
    //? if <= 1.21.1 {
    sendSystemMessage(Text.literal(s))
    //?} else {
    /*displayClientMessage(Text.literal(s), false)
    *///?}
}

fun Level.setBlockNoPP(pos: BlockPos, state: BlockState, flags: Int = Block.UPDATE_CLIENTS) {
//    setBlockState(pos, state, flags and Block.NOTIFY_NEIGHBORS.inv() or Block.FORCE_STATE or Block.SKIP_DROPS)
    val stateBefore = getBlockState(pos)
    if (stateBefore.hasBlockEntity()) {
        removeBlockEntity(pos)
    }
    getChunk(pos).run { getSection(getSectionIndex(pos.y)) }
        .setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state, false)
    getChunkAt(pos).run {
        this.heightmaps[Heightmap.Types.MOTION_BLOCKING]!!.update(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Types.MOTION_BLOCKING_NO_LEAVES]!!.update(
            pos.x and 15,
            pos.y,
            pos.z and 15,
            state
        )
        this.heightmaps[Heightmap.Types.OCEAN_FLOOR]!!.update(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Types.WORLD_SURFACE]!!.update(pos.x and 15, pos.y, pos.z and 15, state)
        //? if <= 1.21.1 {
        isUnsaved = true
        //?} else {
        /*markUnsaved()
        *///?}

        //? if <= 1.21.1 {
        if (LightEngine.hasDifferentLightProperties(this, pos, stateBefore, state)) {
        //?} else {
        /*if (LightEngine.hasDifferentLightProperties(stateBefore, state)) {
        *///?}
            skyLightSources.update(this, pos.x and 15, pos.y and 15, pos.z and 15)
            chunkSource.lightEngine.checkBlock(pos)
        }

        if (!state.`is`(stateBefore.block) && stateBefore.hasBlockEntity()) {
            this.removeBlockEntity(pos)
        }

        if (state.hasBlockEntity()) {
            var blockEntity = this.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK)
            if (blockEntity == null) {
                blockEntity = (state.block as EntityBlock).newBlockEntity(pos, state)
            }
            if (blockEntity != null) {
                this.setBlockEntity(blockEntity)
            }
        }
    }
    if (this is ServerLevel) {
        chunkSource.blockChanged(pos)
    }
    if (flags and Block.UPDATE_CLIENTS != 0) {
        sendBlockUpdated(pos, stateBefore, state, flags)
    }

    //? if < 1.21.5 {
    this.onBlockStateChange(pos, stateBefore, state)
    //?} else {
    /*this.updatePOIOnBlockStateChange(pos, stateBefore, state)
    *///?}
}

val isClient: Boolean get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT

object ResourceLoader {
    fun loadBytes(path: String): ByteArray? {
        return Reden::class.java.classLoader.getResourceAsStream(path)?.readAllBytes()
    }

    fun loadString(path: String): String {
        return loadBytes(path)!!.decodeToString()
    }

    fun loadStringOrNull(path: String): String? {
        return loadBytes(path)?.decodeToString()
    }

    @JvmStatic
    fun loadLang(lang: String) =
        loadStringOrNull("assets/reden/lang/$lang.json")?.let {
            // work around for owo rich translate
            @Suppress("UNCHECKED_CAST")
            Gson().fromJson(it, Map::class.java).filterValues { value -> value is String } as Map<String, String>
        }
}

fun memorySizeToString(size: Int) {
    val unit = arrayOf("B", "KB", "MB", "GB", "TB")
    var i = 0
    var s = size.toDouble()
    while (s > 1024) {
        s /= 1024
        i++
    }
    println("%.2f".format(s) + unit[i])
}

fun MutableComponent.red() = withStyle(ChatFormatting.RED)!!

fun URL.openStreamRetrying(retries: Int = 3): InputStream {
    var retry = retries
    while (retry > 0) {
        try {
            return this.openStream()
        } catch (e: IOException) {
            Reden.LOGGER.warn("Opening $this", e)
        }
        retry--
    }
    Reden.LOGGER.error("Opening $this: max retries exceeded.")
    throw IOException("Opening $this: max retries exceeded.")
}

fun checkMalilib() {
    try {
        if (isClient)
            Class.forName("fi.dy.masa.malilib.util.FileUtils")
    } catch (_: ClassNotFoundException) {
        throw ModResolutionException(
            """
            Dependency not found!
            Reden requires Malilib to run on the clients.
            Please install Malilib from https://www.curseforge.com/minecraft/mc-mods/malilib
        """.trimIndent()
        )
    }
}

/**
 * @author Zai_yu_you
 */
@Deprecated("", level = DeprecationLevel.HIDDEN)
fun generateRandomColor(alpha: Int, baseGray: Int, offsetWeight: Float): Int {
    require(offsetWeight > 0 && offsetWeight <= 1) { "The input offsetWeight must be between 0(inclusive) and 1 " }
    require(baseGray in 1..256) { "The input baseGray must be between 0(inclusive) and 256 " }
    val random = Random()

    var r = (baseGray * (1 - offsetWeight) + random.nextInt((baseGray * offsetWeight).toInt())).toInt()
    var g = (baseGray * (1 - offsetWeight) + random.nextInt((baseGray * offsetWeight).toInt())).toInt()
    var b = (baseGray * (1 - offsetWeight) + random.nextInt((baseGray * offsetWeight).toInt())).toInt()

    //归一化
    var scaleFactor = 256f / (r + g + b)
    r = (r * scaleFactor).toInt()
    g = (g * scaleFactor).toInt()
    b = (b * scaleFactor).toInt()

    // 调整RGB值，使其灰度接近于目标灰度
    val currentGray = (0.2126f * r + 0.587f * g + 0.114f * b).toInt()
    scaleFactor = baseGray.toFloat() / currentGray
    r = (r * scaleFactor).toInt()
    g = (g * scaleFactor).toInt()
    b = (b * scaleFactor).toInt()

    // 确保RGB值在0-255范围内
    r = max(14, min(r, 207))
    g = max(13, min(g, 210))
    b = max(23, min(b, 234))

    // 保留Alpha通道的值
    val a = alpha and 0xFF

    // 确保RGB值在0-255范围内
    r = r and 0xFF
    g = g and 0xFF
    b = b and 0xFF

    // 合并RGB值到ARGB值中
    val rgb = (r shl 16) or (g shl 8) or b

    // 返回合并后的ARGB值
    return (a shl 24) or rgb
}

//val redenApiBaseUrl: String
//    get() = if (isClient && DEVELOPER_MODE.booleanValue) LOCAL_API_BASEURL.stringValue
//    else "https://api.redenmc.com/api"

infix fun Int.has(flag: Int) = (this and flag) == flag

@Suppress("NOTHING_TO_INLINE")
inline fun redenError(message: String, throwable: Throwable? = null, log: Boolean = false): Nothing {
    if (log) {
        Reden.LOGGER.error(message, throwable)
    }
    throw if (throwable != null) RedenException(message, throwable) else RedenException(message)
}

@Suppress("NOTHING_TO_INLINE")
inline fun redenError(message: Component, throwable: Throwable? = null, log: Boolean = false): Nothing {
    if (log) {
        Reden.LOGGER.error(message.string, throwable)
    }
    throw if (throwable != null) RedenException(message, throwable) else RedenException(message)
}

fun Class<*>.shortenName(): String {
    val simple = this.name.substringAfterLast('.')
    return this.name.split('.').dropLast(1).joinToString(".") { it[0].toString() } + "." + simple
}

//? if <= 1.21.1 {
fun MinecraftServer.send(task: () -> Unit) = tell(TickTask(tickCount, task))
//?} else {
/*fun MinecraftServer.send(task: () -> Unit) = schedule(TickTask(tickCount, task))
*///?}

@Suppress("NOTHING_TO_INLINE")
inline fun error(reason: String): Nothing =
    throw SimpleCommandExceptionType(Text.literal(reason)).create()
