package com.github.zly2006.reden.transformers

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
import java.io.File
import kotlin.io.path.createDirectories

class RedenMixinExtension: IExtension {
    private val LOGGER = org.apache.logging.log4j.LogManager.getLogger("Reden/MixinExt")!!
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
                    transformer.transform(node)
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
                    transformer.transformPost(node)
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
    }
}
