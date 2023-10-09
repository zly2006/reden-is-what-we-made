package com.github.zly2006.reden.transformers

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

object MethodBytecodePrinter: MethodVisitor(Opcodes.ASM9) {
    val opcodeMap = mutableMapOf<Int, String>()
    init {
        Opcodes::class.java.fields.dropWhile { it.name != "NOP" }.forEach {
            opcodeMap[it.getInt(null)] = it.name
        }
    }

    fun opcodeName(opcode: Int) = opcodeMap[opcode] ?: "UNKNOWN"

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        println("  ${opcodeName(opcode)} $owner $name $descriptor")
    }

    override fun visitInsn(opcode: Int) {
        println("  ${opcodeName(opcode)}")
    }

    override fun visitIincInsn(varIndex: Int, increment: Int) {
        println("  IINC $varIndex $increment")
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        println("  ${opcodeName(opcode)} $operand")
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        println("  ${opcodeName(opcode)} $label")
    }

    override fun visitLdcInsn(value: Any?) {
        println("  LDC $value")
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
        println("  ${opcodeName(opcode)} $owner $name $descriptor $isInterface")
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        println("  ${opcodeName(opcode)} $type")
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        println("  TABLESWITCH $min $max $dflt " + labels.map { "\n  - $it"}.joinToString(""))
    }

    override fun visitVarInsn(opcode: Int, varIndex: Int) {
        println("  ${opcodeName(opcode)} $varIndex")
    }

    override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
        println("  INVOKEDYNAMIC $name $descriptor $bootstrapMethodHandle " + bootstrapMethodArguments.map { "\n  - $it"}.joinToString(""))
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
        println("  MULTIANEWARRAY $descriptor $numDimensions")
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        println("  LOOKUPSWITCH $dflt" + keys?.mapIndexed { index, i ->
            "\n  - $i -> ${labels?.get(index)}"
        }?.joinToString(""))
    }

    override fun visitLabel(label: Label?) {
        println("LABEL $label")
    }

    override fun visitLineNumber(line: Int, start: Label?) {
        println(" LINENUMBER $line $start")
    }
}
