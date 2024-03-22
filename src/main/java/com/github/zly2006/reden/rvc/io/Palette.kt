package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.tracking.io.IRvcFileReader
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper

class Palette {
    private val palette = mutableMapOf<String, Int>()
    val idToName = mutableMapOf<Int, String>()
    private val idToNbt = mutableMapOf<Int, NbtCompound>()

    fun getId(name: String): Int {
        if (name !in palette) {
            val id = palette.size + 1
            palette[name] = id
            idToName[id] = name
            return id
        }
        return palette[name]!!
    }

    fun getName(id: Int): String {
        return idToName[id]
            ?: throw IllegalArgumentException("Palette does not contain id $id")
    }

    fun getNbt(id: Int): NbtCompound {
        if (id !in idToNbt) {
            idToNbt[id] = NbtHelper.fromNbtProviderString(getName(id))
        }
        return idToNbt[id]!!
    }

    companion object {
        fun load(file: IRvcFileReader.RvcFile?): Palette {
            if (file == null) return Palette()
            val palette = Palette()
            file.data.forEach {
                val id = it.substringBefore(",").toInt()
                val name = it.substringAfter(",")
                palette.palette[name] = id
                palette.idToName[id] = name
            }
            return palette
        }
    }
}
