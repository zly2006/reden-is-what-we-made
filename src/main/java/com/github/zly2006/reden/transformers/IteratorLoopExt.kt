package com.github.zly2006.reden.transformers

import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext

class IteratorLoopExt: IExtension {
    override fun checkActive(environment: MixinEnvironment): Boolean {
        println(environment)
        return true
    }

    override fun preApply(context: ITargetClassContext) {
        // do nothing
    }

    override fun postApply(context: ITargetClassContext) {
        println("Post apply: " + context.classInfo.name)
        context.classNode.interfaces.add("com/github/zly2006/reden/transformers/ThisIsReden")
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
        if (force) {
            throw RuntimeException("WTF???")
        }
        // do nothing
    }
}
