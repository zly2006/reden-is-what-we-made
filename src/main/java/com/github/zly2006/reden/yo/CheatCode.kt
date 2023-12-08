package com.github.zly2006.reden.yo

import com.github.zly2006.reden.utils.sendMessage
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

class CheatCode(
    val codeSequence: List<Int>,
    val action: () -> Unit,
) {
    private var index = 0
    fun keyPressed(key: Int) {
        if (key == codeSequence[index]) {
            index++
            if (index == codeSequence.size) {
                action()
                index = 0
            }
        } else {
            index = 0
        }
    }

    companion object {
        val cheatCodes = mutableListOf<CheatCode>()
        fun register(code: CheatCode) {
            cheatCodes.add(code)
        }
        @JvmStatic
        fun onKeyPressed(key: Int) {
            cheatCodes.forEach {
                it.keyPressed(key)
            }
        }

        init {
            register(CheatCode(listOf(
                GLFW.GLFW_KEY_UP,
                GLFW.GLFW_KEY_UP,
                GLFW.GLFW_KEY_DOWN,
                GLFW.GLFW_KEY_DOWN,
                GLFW.GLFW_KEY_LEFT,
                GLFW.GLFW_KEY_RIGHT,
                GLFW.GLFW_KEY_LEFT,
                GLFW.GLFW_KEY_RIGHT,
                GLFW.GLFW_KEY_B,
                GLFW.GLFW_KEY_A,
            )) {
                val mc = MinecraftClient.getInstance()
                mc.player?.sendMessage("Hello, world!")
            })
            register(CheatCode(listOf(
                // This is Reden
                GLFW.GLFW_KEY_T,
                GLFW.GLFW_KEY_H,
                GLFW.GLFW_KEY_I,
                GLFW.GLFW_KEY_S,
                GLFW.GLFW_KEY_I,
                GLFW.GLFW_KEY_S,
                GLFW.GLFW_KEY_R,
                GLFW.GLFW_KEY_E,
                GLFW.GLFW_KEY_D,
                GLFW.GLFW_KEY_E,
                GLFW.GLFW_KEY_N,
            )) {
                val mc = MinecraftClient.getInstance()
                mc.player?.sendMessage("This is Reden!")
            })
        }
    }
}
