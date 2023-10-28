package com.github.zly2006.reden.transformers

import com.github.zly2006.reden.asm.FabricLoaderInjector
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
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
         * If you want to add code to the end/begin of the method, you should override [transformPost].
         */
        abstract fun transform(node: MethodNode)

        /**
         * This method will be called after all mixins are applied.
         *
         * If you want to add code to the end/begin of the method, you should override this method.
         */
        abstract fun transformPost(node: MethodNode)
    }

    val targetList = mutableListOf(
        ClassToTransform( // Just for test
            "com/mojang/brigadier/CommandDispatcher",
        ) { listOf() },
    )

    val targets = targetList.associateBy { it.mappedName }
}
