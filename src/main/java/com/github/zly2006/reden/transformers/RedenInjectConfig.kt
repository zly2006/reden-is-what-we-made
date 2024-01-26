package com.github.zly2006.reden.transformers

import com.github.zly2006.reden.asm.FabricLoaderInjector
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object RedenInjectConfig {
    val injector = FabricLoaderInjector(this::class.java.classLoader)
    open class Target (
        val interName: String,
    )

    open class ClassToTransform(
        interName: String,
        methodTransformers: ClassToTransform.() -> List<MethodToTransform>,
    ): Target(interName) {
        lateinit var node: ClassNode
        val mappedName = FabricLoader.getInstance().mappingResolver
                .mapClassName("intermediary", interName.replace('/', '.'))
                .replace('.', '/')

        fun mapIntermediaryMethodName(name: String) =
            FabricLoader.getInstance().mappingResolver
                .mapMethodName(
                    "intermediary",
                    interName.replace('/', '.'),
                    name.substringBefore('('),
                    name.substring(name.indexOf('('))
                )!!

        val methodTransformers = lazy {
            methodTransformers().associateBy {
                IntermediaryMappingAccess.getMethodOrDefault(interName, it.interName).name
            }
        }
    }

    abstract class MethodToTransform(
        name: String
    ): Target(name) {
        /**
         * This method will be called before all mixins are applied.
         *
         * If you want to add code to the end/begin of the method, you should override [post].
         */
        abstract fun pre(node: MethodNode)

        /**
         * After the mixin with priority [com.github.zly2006.reden.Reden.REDEN_HIGHEST_MIXIN_PRIORITY] applied
         */
        abstract fun afterRedenMixin(node: MethodNode)

        /**
         * This method will be called after all mixins are applied.
         *
         * If you want to add code to the end/begin of the method, you should override this method.
         */
        abstract fun post(node: MethodNode)
    }

    class SplitHeadTailTransformer(name: String) : MethodToTransform(name) {
        val head = InsnList()
        val tail = InsnList()
        override fun pre(node: MethodNode) {
        }
        override fun afterRedenMixin(node: MethodNode) {
            val hints = node.instructions.filter {
                it is MethodInsnNode
                        && it.owner == "com/github/zly2006/reden/transformers/Helper"
                        && it.name == "transformerHint"
                        && it.desc == "(Ljava/lang/String;)V"
            }.map { it as MethodInsnNode }
            if (hints.size != 2) {
                throw IllegalStateException("Cannot find transformer hints")
            }
            val array = node.instructions.toArray()
            val indexHead = node.instructions.indexOf(hints[0])
            val indexTail = node.instructions.indexOf(hints[1])
            for (i in 0 until indexHead) {
                head.add(array[i])
            }
            for (i in indexTail + 1 until array.size) {
                tail.add(array[i])
            }
            node.instructions.clear()
            for (i in indexHead + 1 until indexTail) {
                node.instructions.add(array[i])
            }
        }

        override fun post(node: MethodNode) {
            node.instructions.insert(head)
            node.instructions.add(tail)
        }
    }

    val targetList = mutableListOf(
        ClassToTransform(
            // Just for test
            "com/mojang/brigadier/CommandDispatcher",
        ) { listOf() }
    )

    val targets = targetList.associateBy { it.mappedName }
}
