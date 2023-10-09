package com.github.zly2006.reden.transformers

import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext

class IteratorLoopExt: IExtension {
    val LOGGER = org.apache.logging.log4j.LogManager.getLogger("Reden/MixinExt")
    override fun checkActive(environment: MixinEnvironment): Boolean {
        println(environment)
        return true
    }

    override fun preApply(context: ITargetClassContext) {
        val classToTransform = RedenInjectConfig.targets[context.classInfo.name]
        if (classToTransform != null) {
            LOGGER.info("Transforming class: " + context.classInfo.name)
            classToTransform.node = context.classNode
            context.classNode.interfaces.add("com/github/zly2006/reden/transformers/ThisIsReden")
            classToTransform.methodTransformers.forEach { (name, transformer) ->
                val node = context.classNode.methods.firstOrNull { it.name == name }
                if (node != null) {
                    LOGGER.info("Transforming method: " + node.name)
                    transformer.transform(node)
                } else {
                    throw RuntimeException("Method not found: ${transformer.interName}")
                }
            }
        }
    }

    override fun postApply(context: ITargetClassContext) {
        val classToTransform = RedenInjectConfig.targets[context.classInfo.name]
        if (classToTransform != null) {
            LOGGER.info("Transformed class: " + context.classInfo.name)
            classToTransform.node = context.classNode
            context.classNode.interfaces.add("com/github/zly2006/reden/transformers/ThisIsReden")
            classToTransform.methodTransformers.forEach { (name, transformer) ->
                val node = context.classNode.methods.firstOrNull { it.name == name }
                if (node != null) {
                    // todo: print
                } else {
                    LOGGER.error("Method not found: ${transformer.interName}")
                }
            }
        }
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
        if (force) {
            throw RuntimeException("WTF???")
        }
        // do nothing
    }
}
