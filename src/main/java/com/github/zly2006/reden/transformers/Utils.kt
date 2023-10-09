package com.github.zly2006.reden.transformers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*


fun debugPrint(thisClass: ClassNode, fieldNode: FieldNode) = InsnList().apply {
    // Java: System.out.println({fieldNode})
    add(LabelNode())
    add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
    if (fieldNode.access and Opcodes.ACC_STATIC == 0) {
        add(VarInsnNode(Opcodes.ALOAD, 0)) // this
        add(FieldInsnNode(Opcodes.GETFIELD, thisClass.name, fieldNode.name, fieldNode.desc)) // get field
    }
    else {
        add(FieldInsnNode(Opcodes.GETSTATIC, thisClass.name, fieldNode.name, fieldNode.desc)) // get field
    }
    if (fieldNode.desc.length == 1) {
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(${fieldNode.desc})V"))
    } else {
        add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V"))
    }
}
fun printString(str: String) = InsnList().apply {
    // Java: System.out.println({str})
    add(LabelNode())
    add(FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"))
    add(LdcInsnNode(str))
    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V"))
}
fun invalidLabel() = InsnList().apply {
    add(LabelNode())
    add(TypeInsnNode(Opcodes.NEW, "java/lang/RuntimeException"))
    add(InsnNode(Opcodes.DUP))
    add(LdcInsnNode("Reden Mod Critical Error: Invalid label on coroutines"))
    add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V"))
    add(InsnNode(Opcodes.ATHROW))
}
fun ClassNode.findMixinMergedField(name: String, mixinPrefix: String = "com.github.zly2006.reden.mixin."): FieldNode {
    val filter = fields.filter {
        it.visibleAnnotations?.any { annotation ->
            if (annotation.desc != "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") {
                return@any false
            }
            val index = annotation.values.indexOf("mixin")
            if (index == -1) {
                return@any false
            }
            val value = annotation.values[index + 1] as? String ?: return@filter false
            return@any value.startsWith(mixinPrefix)
        } == true
    }.filter { it.name.contains(name) }
    if (filter.size != 1) {
        throw RuntimeException("WTF??? = $filter")
    }
    return filter.first()
}
