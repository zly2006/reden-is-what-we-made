package com.github.zly2006.reden.task

import net.minecraft.client.world.ClientWorld

val taskStack = mutableListOf<Task>()

abstract class Task(val id: String) {
    /**
     * By default, enter key pressed
     */
    open fun onConfirm(): Boolean {
        return false
    }

    /**
     * By default, escape key pressed
     */
    open fun onCancel(): Boolean {
        return false
    }

    open fun onClientSideWorldChanged(newWorld: ClientWorld?) {

    }
}
