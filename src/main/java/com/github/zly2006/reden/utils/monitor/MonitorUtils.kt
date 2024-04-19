package com.github.zly2006.reden.utils.monitor

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 *
 * Check annotations:
 * - if system property `reden.monitor` is `false`, return `false`
 * - if it has [com.github.zly2006.reden.utils.monitor.AddMonitor], return `true`
 * - if it has [com.github.zly2006.reden.utils.monitor.DontAddMonitor], return `false`
 * - otherwise, if it has more than 20 instructions, return `true`
 */
fun MethodNode.shouldApplyMonitor(): Boolean {
    if (System.getProperty("reden.monitor", "").equals("false", true)) {
        return false
    }
    invisibleAnnotations?.forEach {
        if (it.desc == "Lcom/github/zly2006/reden/utils/monitor/AddMonitor;") {
            return true
        }
        if (it.desc == "Lcom/github/zly2006/reden/utils/monitor/DontAddMonitor;") {
            return false
        }
    }
    return instructions.size() > 20
}

fun ClassNode.start(method: MethodNode) = InsnList().apply {
    // load this
    add(VarInsnNode(Opcodes.ALOAD, 0))
    // load this function's name
    val name = this@start.name.replace("/", ".") + "#" + method.name
    add(LdcInsnNode(name))
    // invoke static Lcom/github/zly2006/reden/utils/monitor/MonitorUtils;start(Ljava/lang/String;)Z
    add(
        MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "com/github/zly2006/reden/utils/monitor/MonitorUtils",
            "start",
            "(Ljava/lang/String;)Z",
            false
        )
    )
    // save to "boolean this.profileStarted$reden"
    add(FieldInsnNode(Opcodes.PUTFIELD, this@start.name, "profileStarted\$reden", "Z"))

}

fun ClassNode.end() = InsnList().apply {
    // "boolean this.profileStarted$reden"
    add(VarInsnNode(Opcodes.ALOAD, 0))
    add(FieldInsnNode(Opcodes.GETFIELD, name, "profileStarted\$reden", "Z"))
    val elseLabel = LabelNode()
    // if (this.profileStarted$reden) {
    add(JumpInsnNode(Opcodes.IFNE, elseLabel))
    // this.profileStarted$reden = false
    add(VarInsnNode(Opcodes.ALOAD, 0))
    add(InsnNode(Opcodes.ICONST_0))
    add(FieldInsnNode(Opcodes.PUTFIELD, name, "profileStarted\$reden", "Z"))
    // invoke static Lcom/github/zly2006/reden/utils/monitor/MonitorUtils;end()V
    add(
        MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "com/github/zly2006/reden/utils/monitor/MonitorUtils",
            "end",
            "()V",
            false
        )
    )
    // }
    add(elseLabel)
}

fun ClassNode.addProfilerField() {
    fields.add(FieldNode(Opcodes.ACC_PUBLIC, "profileStarted\$reden", "Z", null, false))
}
