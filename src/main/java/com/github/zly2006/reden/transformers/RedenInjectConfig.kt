package com.github.zly2006.reden.transformers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.io.File
import kotlin.io.path.createDirectories

object RedenInjectConfig {
    open class Target (
        val interName: String,
    )

    class ClassToTransform(
        interName: String,
        methodTransformers: ClassToTransform.() -> List<MethodToTransform>,
    ): Target(interName) {
        var node: ClassNode? = null
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

        val methodTransformers = methodTransformers().associateBy { mapIntermediaryMethodName(it.interName) }
    }

    abstract class MethodToTransform(
        name: String
    ): Target(name) {
        abstract fun transform(node: MethodNode)
    }

    val targetList = mutableListOf(
        ClassToTransform( // Just for test
            "com/mojang/brigadier/CommandDispatcher",
        ) { listOf() },
        ClassToTransform("net/minecraft/class_3218") {
            listOf(
                /**
                 * [net.minecraft.server.world.ServerWorld.tick]
                 */
                object : MethodToTransform(
                    "method_18765(Ljava/util/function/BooleanSupplier;)V",
                ) {
                    override fun transform(node: MethodNode) {
                        val classNode = this@ClassToTransform.node!!
                        val field = FieldNode(
                            Opcodes.ACC_PUBLIC,
                            "reden_tickLabel",
                            "I",
                            null,
                            0
                        )
                        classNode.fields.add(field)
                        val labelMap = mutableMapOf<Int, LabelNode>()

                        fun setTickLabel(value: Int) = InsnList().apply {
                            // Java: this.tickLabel = {value}
                            if (labelMap[value] != null) {
                                error("Register label $value twice")
                            }
                            labelMap[value] = LabelNode()
                            add(labelMap[value]!!)

                            add(VarInsnNode(Opcodes.ALOAD, 0)) // this
                            add(LdcInsnNode(value)) // load constant value
                            add(FieldInsnNode(Opcodes.PUTFIELD, mappedName, field.name, field.desc)) // set field
                        }
                        node.instructions.insert(setTickLabel(1))
                        for (insn in node.instructions) {
                            if (insn is MethodInsnNode) {
                                // inject after
                                //   Lnet/minecraft/server/world/ServerWorld;tickWeather()V
                                if (insn.name == mapIntermediaryMethodName("method_39501()V")) {
                                    // end of label 0
                                    // invoke this method again
                                    // fixme: ---DEBUG---
                                    node.instructions.insert(insn, InsnList().apply {
                                        add(printString("Hello"))
                                        add(VarInsnNode(Opcodes.ALOAD, 0))
                                        add(VarInsnNode(Opcodes.ALOAD, 1))
                                        add(MethodInsnNode(
                                            Opcodes.INVOKEVIRTUAL,
                                            mappedName,
                                            mapIntermediaryMethodName("method_18765(Ljava/util/function/BooleanSupplier;)V"),
                                            "(Ljava/util/function/BooleanSupplier;)V"
                                        ))
                                        add(InsnNode(Opcodes.RETURN))
                                        // start of label 1
                                        add(setTickLabel(2))
                                    })
                                }
                            }
                        }

                        fun tryExport() {
                            val exportFile = File("export/${classNode.name}.class")
                            exportFile.toPath().parent.createDirectories()
                            exportFile.writeBytes(
                                ClassWriter(3).let {
                                    classNode.accept(it)
                                    it.toByteArray()
                                }
                            )
                        }

                        val invalidLabelCode = invalidLabel()
                        val switchNode = LookupSwitchInsnNode(
                            invalidLabelCode.first as LabelNode,
                            // by this mapping, if label is 0 we jump to 1, 1 -> 2, etc.
                            labelMap.keys.map { it - 1 }.toIntArray(),
                            labelMap.values.toTypedArray()
                        )
                        // add our error block and switch at last
                        node.instructions.add(invalidLabelCode)
                        tryExport()
                        // add our switch at first
                        node.instructions.insert(InsnList().apply {
                            add(LabelNode())
                            add(VarInsnNode(Opcodes.ALOAD, 0)) // this
                            add(FieldInsnNode(Opcodes.GETFIELD, classNode.name, field.name, field.desc)) // get field
                            add(switchNode)
                        })
                        tryExport()
                    }
                }
            )
        },
    )

    val targets = targetList.associateBy { it.mappedName }
}
