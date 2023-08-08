package com.github.zly2006.reden.pearl

import com.github.zly2006.reden.utils.buttonWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class PearlScreen: Screen(Text.of("Pearl"))/*, IKeybindConfigGui */{
    val startRecordingButton = buttonWidget(0, 0, 100, 20, Text.of("Start Recording")) {

    }

    override fun init() {
        addDrawableChild(startRecordingButton)
        val task = pearlTask.let {
            pearlTask = PearlTask()
            pearlTask!!
        }
        if (task.mode == PearlTask.Mode.NOT_INITIALIZED) {
            task.mode = PearlTask.Mode.RECORDING
        }
    }
}
