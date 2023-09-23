package com.redenmc.mineroutine

class RunStack {
    val frames: MutableList<Frame> = mutableListOf()
    val currentFrame: Frame?
        get() = frames.lastOrNull()
}
