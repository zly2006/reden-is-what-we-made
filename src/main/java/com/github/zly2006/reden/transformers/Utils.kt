package com.github.zly2006.reden.transformers

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree.ClassMapping
import net.minecraft.server.MinecraftServer
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


class MethodInfo(
    val owner: String,
    val name: String,
    val desc: String,
) {
    fun toInsn(opcode: Int) = MethodInsnNode(opcode, owner, name, desc)
}

object IntermediaryMappingAccess {
    val mapping = FabricLauncherBase.getLauncher().mappingConfiguration.getMappings()!!
    private val classMapping2default: Map<String, String> = mapping.run {
        classes.associate {
            it.getName("intermediary")!! to it.getName(srcNamespace)!!
        }
    }

    private val methodMapping = mutableMapOf<String, MethodInfo>()

    /**
     * Get name info in current namespace by an intermediary method name.
     */
    fun getMethod(owner: String?, name: String): MethodInfo? {
        if (owner?.contains('.') == true) {
            throw IllegalArgumentException("Found '.' in owner: $owner, do you mean ${owner.replace('.', '/')}?")
        }
        if (name in methodMapping) {
            return methodMapping[name]!!
        }
        val targetNamespace = FabricLauncherBase.getLauncher().mappingConfiguration.targetNamespace
        fun addMethods(classDef: ClassMapping) {
            for (methodDef in classDef.methods) {
                methodMapping.computeIfAbsent(
                    methodDef.getName("intermediary")!!
                ) {
                    MethodInfo(
                        classDef.getName(targetNamespace)!!,
                        methodDef.getName(targetNamespace)!!,
                        methodDef.getDesc(mapping.getNamespaceId(targetNamespace))!!
                    )
                }
            }
        }
        for (classDef in mapping.classes) {
            addMethods(classDef)
        }
        return methodMapping[name]
    }
    fun getMethodOrDefault(owner: String, name: String): MethodInfo {
        return getMethod(owner, name) ?: MethodInfo(owner, name, "")
    }
}

fun getMappedMethod(info: MethodInfo): MethodInfo {
    val name = FabricLoader.getInstance().mappingResolver.mapMethodName(
        "intermediary",
        info.owner.replace('/', '.'),
        info.name,
        info.desc
    )
    FabricLauncherBase.getLauncher().mappingConfiguration.getMappings()
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

fun MinecraftServer.sendToAll(packet: FabricPacket) {
    playerManager.playerList.forEach {
        ServerPlayNetworking.send(it, packet)
    }
}
