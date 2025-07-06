package com.github.zly2006.reden.mixinhelper

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.mixin.transformer.IMixinTransformer
import org.spongepowered.asm.mixin.transformer.ext.Extensions
import org.spongepowered.asm.mixin.transformer.ext.IExtension
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext

class RedenServersideMixinPlugin : IExtension,  IMixinConfigPlugin {
    companion object {
        @JvmField
        val APPLY_DEBUGGER_MIXINS = System.getProperty("reden.debugger", "true").toBoolean()
        private val LOGGER = LogManager.getLogger("Reden/MixinExt")!!
        val finalNodes = mutableMapOf<String, ClassNode>()
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

    override fun preApply(context: ITargetClassContext) {
    }

    private fun getTempValueExpression(
        methodInsn: MethodInsnNode,
    ): MutableList<AbstractInsnNode> {
        // 获取参数类型
        val argTypes = org.objectweb.asm.Type.getArgumentTypes(methodInsn.desc)
        val paramCount = argTypes.size
        // 找到methodInsn之前的指令
        val stack = mutableListOf<AbstractInsnNode>()
        var current: AbstractInsnNode? = methodInsn.previous
        // 记录需要的参数数量
        var needed = paramCount
        // 向前遍历，收集参数的指令
        while (current != null && needed > 0) {
            // 跳过Label/LineNumber等
            if (current !is LabelNode && current !is LineNumberNode) {
                stack.add(0, current)
                when (current.opcode) {
                    in Opcodes.ILOAD..Opcodes.ALOAD -> needed--
                    in Opcodes.BIPUSH..Opcodes.LDC -> needed--
                    in Opcodes.GETSTATIC..Opcodes.GETFIELD -> needed--
                    in Opcodes.INVOKEVIRTUAL..Opcodes.INVOKESTATIC -> {
                        val desc = when (current) {
                            is MethodInsnNode -> current.desc
                            else -> null
                        }
                        if (desc != null) {
                            val types = org.objectweb.asm.Type.getArgumentTypes(desc)
                            needed -= types.size
                            if (current.opcode != Opcodes.INVOKESTATIC) needed--
                        }
                    }
                    else -> {}
                }
            }
            current = current.previous
        }
        return stack
    }

    override fun postApply(context: ITargetClassContext) {
        if (context.classNode.name == "net/minecraft/server/commands/CloneCommands") {
            LOGGER.warn("Found CloneCommands in ${context.classNode.name}, this class is not supported by Reden, please report this to the Reden team.")
        }
        context.classNode.methods.forEach { method ->
            val instructions = method.instructions.toList()
            instructions.forEach {
                if (it is MethodInsnNode  && it.desc == "()V" && (it.name.equals("method_31663") || (
                            it.owner.contains("BlockEntity") &&
                            it.name.equals("setChanged")))) {
                    LOGGER.info("Found setChanged in ${context.classNode.name}.${method.name}, injecting Reden undo monitor.")
                    val prev = it.previous
                    val invoke = MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "com/github/zly2006/reden/mixinhelper/UndoMixinHelper",
                        "monitorSetBlock",
                        "(Ljava/lang/Object;)V",
                        false
                    )
                    when {
                        prev is VarInsnNode -> {
                            val stores =
                                instructions.filter { it is VarInsnNode && it.`var` == prev.`var` && it.opcode == Opcodes.ASTORE }
                            if (stores.isEmpty()) {
                                method.instructions.insertBefore(prev, LabelNode())
                                method.instructions.insertBefore(prev, VarInsnNode(
                                    Opcodes.ALOAD, prev.`var`
                                ))
                                method.instructions.insertBefore(prev, invoke)
                            } else if (stores.size > 1) {
                                LOGGER.warn("setChanged: stores.size > 1, this may not supported by Reden, please report this to the Reden team. Only method without jumps can be supported.")
                                stores.last().let { store ->
                                    method.instructions.insertBefore(store, InsnNode(Opcodes.DUP))
                                    method.instructions.insertBefore(store, invoke)
                                }
                            } else {
                                val store = stores.first()
                                method.instructions.insertBefore(store, InsnNode(Opcodes.DUP))
                                method.instructions.insertBefore(store, invoke)
                            }
                        }
                        prev is FieldInsnNode && prev.opcode == Opcodes.GETFIELD -> {
                            //TODO
                            LOGGER.error("setChanged: FieldInsnNode with GETFIELD is not supported by Reden, please report this to the Reden team. This may cause issues in undo/redo.")
                            val firstFieldNode = instructions.firstOrNull { it is FieldInsnNode && it.opcode == Opcodes.GETFIELD && it.name == prev.name }
//                            method.instructions.insert(firstFieldNode, InsnNode(Opcodes.DUP))
//                            method.instructions.insert(firstFieldNode, invoke)
                            method.instructions.insertBefore(
                                method.instructions.first,
                                LabelNode()
                            )
//                            method.instructions.insertBefore(
//                                method.instructions.first,
//                                prev.clone(null)
//                            )
                            method.instructions.insertBefore(
                                method.instructions.first,
                                LabelNode()
                            )
                            method.maxStack += 6
                            method.maxLocals += 4
                        }
                        else -> {
                            LOGGER.error("This is likely a call to setChanged on an unknown object, which is not supported by Reden.")
                        }
                    }
                }
            }
        }
    }

    override fun export(env: MixinEnvironment, name: String, force: Boolean, classNode: ClassNode) {
        finalNodes[classNode.name] = classNode
    }

    override fun onLoad(mixinPackage: String) { }

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName.startsWith("com.github.zly2006.reden.mixin.debugger."))
            return APPLY_DEBUGGER_MIXINS
        if (mixinClassName.startsWith("com.github.zly2006.reden.mixin.") && mixinClassName.contains(".otherMods.")) {
            val modId = mixinClassName.split(".").dropWhile { it != "otherMods" }.drop(1).firstOrNull() ?: return true
            return FabricLoader.getInstance().isModLoaded(modId)
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {
    }

    override fun getMixins() = null

    override fun preApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
    }

    override fun postApply(targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo) {
    }
}
