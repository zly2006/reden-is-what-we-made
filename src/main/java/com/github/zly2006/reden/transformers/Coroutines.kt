package com.github.zly2006.reden.transformers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

fun generateContinuation(
    name: String,
    invokeSuspend: MethodNode.() -> Unit,
): ClassNode {
    val classNode = ClassNode(Opcodes.ASM9)
    classNode.visit(
        Opcodes.V17,
        Opcodes.ACC_PUBLIC,
        name,
        null,
        "kotlin/coroutines/jvm/internal/ContinuationImpl",
        arrayOf("kotlin/coroutines/Continuation")
    )
    classNode.methods.add(MethodNode(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "(Lkotlin/coroutines/Continuation;)V",
        null,
        null
    ).apply {
        instructions = InsnList().apply {
            add(VarInsnNode(Opcodes.ALOAD, 0))
            add(VarInsnNode(Opcodes.ALOAD, 1))
            add(MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "kotlin/coroutines/jvm/internal/ContinuationImpl",
                "<init>",
                "(Lkotlin/coroutines/Continuation;)V"
            ))
            add(InsnNode(Opcodes.RETURN))
        }
        maxStack = 2
        maxLocals = 2
    })
    classNode.methods.add(MethodNode(
        Opcodes.ACC_PUBLIC,
        "invokeSuspend",
        "(Ljava/lang/Object;)Ljava/lang/Object;",
        null,
        null
    ).apply {
        instructions.add(MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "kotlin/coroutines/intrinsics/IntrinsicsKt",
            "getCOROUTINE_SUSPENDED",
            "()Ljava/lang/Object;"
        ))
        // local 0: this
        // local 1: result
        // local 2: COROUTINE_SUSPENDED
        instructions.add(VarInsnNode(Opcodes.ASTORE, 2))
        invokeSuspend()
    })
    return classNode
}

fun startContinuation(continuationIndex: Int, scopeIndex: Int, continuation: ClassNode) = InsnList().apply {
    add(InsnNode(Opcodes.ACONST_NULL))
    add(TypeInsnNode(Opcodes.NEW, continuation.name))
    add(InsnNode(Opcodes.DUP))
    add(InsnNode(Opcodes.ACONST_NULL))
    add(MethodInsnNode(
        Opcodes.INVOKESPECIAL,
        continuation.name,
        "<init>",
        "(Lkotlin/coroutines/Continuation;)V"
    ))
    add(VarInsnNode(Opcodes.ASTORE, continuationIndex))
}
