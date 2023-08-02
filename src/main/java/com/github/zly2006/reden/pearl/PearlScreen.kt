package com.github.zly2006.reden.pearl

import com.github.zly2006.reden.utils.buttonWidget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class PearlScreen: Screen(Text.of("Pearl")) {
    val startRecordingButton = buttonWidget(0, 0, 0, 0, Text.of("Start Recording")) {

    }

    override fun init() {
        addDrawableChild(startRecordingButton)
    }
}
