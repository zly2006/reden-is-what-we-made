package com.github.zly2006.reden.transformers

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.transformers.mapping.Intermediary
import com.github.zly2006.reden.transformers.mapping.Yarn
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.mixin.transformer.IMixinTransformer
import org.spongepowered.asm.mixin.transformer.ext.Extensions
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
import java.io.File
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.*

class RedenMixinExtension: IExtension, IMixinConfigPlugin {
    companion object {
        @JvmField
        val APPLY_DEBUGGER_MIXINS = System.getProperty("reden.debugger", "true").toBoolean()
        private val LOGGER = LogManager.getLogger("Reden/MixinExt")!!
        val finalNodes = mutableMapOf<String, ClassNode>()

        /**
         * Fabric intermediary mapping
         */
        const val INTERMEDIARY = "intermediary"
        /**
         * Official obfuscated mapping
         */
        const val OFFICIAL_OBFUSCATED = "official"
        /**
         * Yarn mapping
         */
        const val YARN = "named"

        fun exportClasses(path: Path) {
            val mapping = Intermediary(Path.of("."))
            val yarn = Yarn(Path.of("."))
            val intermediaryClientPath = Path(URLDecoder.decode(
                MinecraftServer::class.java.protectionDomain.codeSource.location.path,
                Charset.defaultCharset()
            ))
            LOGGER.info("Downloading intermediary -> yarn mapping")
            mapping.download()
            yarn.download()
            val tinyTree = mapping.load()
            val yarnTree = yarn.load()
/*
            if (!intermediaryClientPath.exists()) {
                val mcRemapper = TinyRemapper.newRemapper()
                    .withMappings(TinyRemapperMappingsHelper.create(tinyTree, OFFICIAL_OBFUSCATED, INTERMEDIARY))
                    .build()
                val mcTag = mcRemapper.createInputTag()
                val clientPath =
                    URLDecoder.decode(MinecraftServer::class.java.protectionDomain.codeSource.location.path, Charset.defaultCharset())
                mcRemapper.readInputs(
                    mcTag,
                    Path(MinecraftServer::class.java.protectionDomain.codeSource.location.path)
                )
                val mcOutput = OutputConsumerPath.Builder(intermediaryClientPath)
                    .assumeArchive(true)
                    .build()
                mcRemapper.apply(mcOutput, mcTag)
            }

 */

            finalNodes.forEach { (name, node) ->
                try {
                    val classWriter = ClassWriter(3)
                    node.accept(classWriter)
                    val file = path / "unmapped" / "$name.class"
                    file.parent.createDirectories()
                    file.writeBytes(classWriter.toByteArray())
                } catch (e: Exception) {
                    LOGGER.error("Failed to export class $name", e)
                }
            }
            TODO()
            val output = OutputConsumerPath.Builder(path / "mapped")
                .build()
            val remapper = TinyRemapper.newRemapper()
                .withMappings(TinyRemapperMappingsHelper.create(yarnTree, INTERMEDIARY, YARN))
                .build()
            val moaTag = remapper.createInputTag()
            if (intermediaryClientPath.exists()) {
                remapper.readClassPath(intermediaryClientPath)
            }
            else {
                LOGGER.warn("Intermediary client jar not found, skipping")
            }
            remapper.readInputs(moaTag, path / "unmapped")
            remapper.apply(output, moaTag)
        }
    }
    init {
        // register self as an extension
        val mGetTransformer = MixinServiceKnot::class.java.getDeclaredMethod("getTransformer")
        mGetTransformer.setAccessible(true)
        val transformer = mGetTransformer.invoke(null) as IMixinTransformer
        (transformer.extensions as Extensions).add(this)
    }

    override fun checkActive(environment: MixinEnvironment): Boolean {
        return true
    }

    init {
        File("reden-transformer-export").deleteRecursively()
    }

    override fun preApply(context: ITargetClassContext) {
        val classToTransform = RedenInjectConfig.targets[context.classInfo.name]
        if (classToTransform != null) {
            LOGGER.info("Transforming class: " + context.classInfo.name)
            classToTransform.node = context.classNode
            context.classNode.interfaces.add("com/github/zly2006/reden/transformers/ThisIsReden")
            classToTransform.methodTransformers.value.forEach { (name, transformer) ->
                val node = context.classNode.methods.firstOrNull { it.name == name }
                if (node != null) {
                    LOGGER.info("Transforming method: " + node.name)
                    transformer.pre(node)
                } else {
                    throw RuntimeException("Method not found: ${transformer.interName}")
                }
            }
            if (System.getProperty("reden.transformer.export.pre") == "true") {
                val classWriter = ClassWriter(3)
                context.classNode.accept(classWriter)
                val file = File("reden-transformer-export/pre/${context.classInfo.name}.class")
                file.toPath().parent.createDirectories()
                file.writeBytes(classWriter.toByteArray())
            }
        }
    }

    override fun postApply(context: ITargetClassContext) {
        val classToTransform = RedenInjectConfig.targets[context.classInfo.name]
        if (classToTransform != null) {
            LOGGER.info("Transformed class: " + context.classInfo.name)
            classToTransform.methodTransformers.value.forEach { (name, transformer) ->
                val node = context.classNode.methods.firstOrNull { it.name == name }
                if (node != null) {
                    LOGGER.info("Post transforming method: " + node.name)
                    transformer.post(node)
                    if (System.getProperty("reden.transformer.printBytecode") == "true") {
                        println("===*** Bytecode of method ${node.name} ***===")
                        node.accept(MethodBytecodePrinter)
                    }
                } else {
                    LOGGER.error("Method not found: ${transformer.interName}")
                }
            }
            if (System.getProperty("reden.transformer.export.post") == "true") {
                val classWriter = ClassWriter(3)
                context.classNode.accept(classWriter)
                val file = File("reden-transformer-export/post/${context.classInfo.name}.class")
                file.toPath().parent.createDirectories()
                file.writeBytes(classWriter.toByteArray())
            }
        }
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
        finalNodes[name] = classNode
    }

    override fun onLoad(mixinPackage: String) { }

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName.startsWith("com.github.zly2006.reden.mixin.debugger."))
            return APPLY_DEBUGGER_MIXINS
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun getMixins() = null

    override fun preApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
    }

    override fun postApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
        val classToTransform = RedenInjectConfig.targets[targetClass.name]
        if (mixinInfo.priority == Reden.REDEN_HIGHEST_MIXIN_PRIORITY && classToTransform != null) {
            LOGGER.info("Ohh, " + mixinInfo.className + " is the highest priority mixin, I'm gonna transform it first")

            LOGGER.info("Transforming class after Reden mixin: " + targetClass.name)
            classToTransform.node = targetClass
            classToTransform.methodTransformers.value.forEach { (name, transformer) ->
                val node = targetClass.methods.firstOrNull { it.name == name }
                if (node != null) {
                    LOGGER.info("Transforming method: " + node.name)
                    transformer.afterRedenMixin(node)
                } else {
                    throw RuntimeException("Method not found: ${transformer.interName}")
                }
            }
            if (System.getProperty("reden.transformer.export.pre") == "true") {
                val classWriter = ClassWriter(3)
                targetClass.accept(classWriter)
                val file = File("reden-transformer-export/pre/${targetClass.name}.class")
                file.toPath().parent.createDirectories()
                file.writeBytes(classWriter.toByteArray())
            }
        }
    }
}
