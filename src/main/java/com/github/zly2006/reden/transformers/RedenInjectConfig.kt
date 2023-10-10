package com.github.zly2006.reden.transformers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

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
        ClassToTransform("net/minecraft/class_3218") {
            listOf(
                /**
                 * [net.minecraft.server.world.ServerWorld.tick]
                 */
                object : MethodToTransform(
                    "method_18765(Ljava/util/function/BooleanSupplier;)V",
                ) {
                    val labelMap = mutableMapOf<Int, LabelNode>()
                    val field = FieldNode(Opcodes.ACC_PUBLIC, "reden_tickLabel", "I", null, 0)
                    override fun transform(node: MethodNode) {
                        val classNode = this@ClassToTransform.node!!
                        classNode.fields.add(field)

                        fun setTickLabel(value: Int, addLabel: Boolean = true) = InsnList().apply {
                            // Java: this.tickLabel = {value}
                            if (addLabel) {
                                if (labelMap[value] != null) {
                                    error("Register label $value twice")
                                }
                                labelMap[value] = LabelNode()
                                add(labelMap[value]!!)
                            }
                            add(VarInsnNode(Opcodes.ALOAD, 0)) // this
                            add(LdcInsnNode(value)) // load constant value
                            add(FieldInsnNode(Opcodes.PUTFIELD, mappedName, field.name, field.desc)) // set field
                        }
                        class MethodInfo(
                            val owner: String,
                            val name: String,
                            val desc: String,
                        ) {
                            fun toInsn(opcode: Int) = MethodInsnNode(opcode, owner, name, desc)
                        }
                        fun getMappedMethod(info: MethodInfo): MethodInfo {
                            val name = FabricLoader.getInstance().mappingResolver.mapMethodName(
                                "intermediary",
                                info.owner.replace('/', '.'),
                                info.name,
                                info.desc
                            )
                            var desc = info.desc
                            val regex = Regex("""L([^;]+);""")
                            val matches = regex.findAll(desc)
                            for (match in matches) {
                                val mapped = FabricLoader.getInstance().mappingResolver.mapClassName(
                                    "intermediary",
                                    match.groupValues[1].replace('/', '.')
                                ).replace('.', '/')
                                desc = desc.replace(match.value, "L$mapped;")
                            }
                            return MethodInfo(
                                FabricLoader.getInstance().mappingResolver.mapClassName(
                                    "intermediary",
                                    info.owner.replace('/', '.')
                                ).replace('.', '/'),
                                name,
                                desc
                            )
                        }
                        fun getProfiler() = InsnList().apply {
                            val method = getMappedMethod(
                                MethodInfo(
                                    "net/minecraft/class_1937",
                                    "method_16107",
                                    "()Lnet/minecraft/class_3695;"
                                )
                            )
                            add(LabelNode())
                            add(VarInsnNode(Opcodes.ALOAD, 0))
                            add(method.toInsn(Opcodes.INVOKEVIRTUAL))
                            add(VarInsnNode(Opcodes.ASTORE, 2))
                        }
                        node.instructions.insert(setTickLabel(1))
                        for (insn in node.instructions) {
                            if (insn is MethodInsnNode) {
                                // inject after
                                //   Lnet/minecraft/server/world/ServerWorld;tickWeather()V
                                if (insn.name == mapIntermediaryMethodName("method_39501()V")) {
                                    // end of label 0
                                    node.instructions.insert(insn, InsnList().apply {
                                        add(InsnNode(Opcodes.RETURN))
                                        // start of label 1
                                        add(setTickLabel(2))
                                        add(getProfiler())
                                    })
                                }
                            }
                            if (insn.opcode == Opcodes.RETURN) {
                                // inject before
                                //   return;
                                node.instructions.insertBefore(insn, setTickLabel(0, false))
                            }
                        }
                    }

                    override fun transformPost(node: MethodNode) {
                        val classNode = this@ClassToTransform.node!!

                        // add code for raising error and *switch*
                        val invalidLabelCode = invalidLabel()
                        val switchNode = LookupSwitchInsnNode(
                            invalidLabelCode.first as LabelNode,
                            // by this mapping, if label is 0 we jump to 1, 1 -> 2, etc.
                            labelMap.keys.map { it - 1 }.toIntArray(),
                            labelMap.values.toTypedArray()
                        )
                        // add our error block and switch at last
                        node.instructions.add(invalidLabelCode)
                        // add our switch at first
                        node.instructions.insert(InsnList().apply {
                            add(LabelNode())
                            add(VarInsnNode(Opcodes.ALOAD, 0)) // this
                            add(FieldInsnNode(Opcodes.GETFIELD, classNode.name, field.name, field.desc)) // get field
                            add(switchNode)
                        })

                        // move instructions from vanilla tick to tickInternal
                        val method = MethodNode(
                            Opcodes.ACC_PUBLIC,
                            "reden_tickInternal",
                            "(Ljava/util/function/BooleanSupplier;)V",
                            null,
                            null
                        )
                        classNode.methods.add(method)
                        // clear old method instructions, and add new method instructions
                        method.instructions = node.instructions
                        node.instructions = InsnList()
                        method.localVariables = node.localVariables
                        node.localVariables = listOf()

                        // redirect to our internal method
                        node.instructions.insert(InsnList().apply {
                            repeat(2) {
                                add(VarInsnNode(Opcodes.ALOAD, 0)) // this
                                add(VarInsnNode(Opcodes.ALOAD, 1)) // BooleanSupplier
                                add(
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        classNode.name,
                                        method.name,
                                        method.desc
                                    )
                                )
                            }
                            add(InsnNode(Opcodes.RETURN))
                        })
                    }
                }
            )
        },
    )

    val targets = targetList.associateBy { it.mappedName }
}
