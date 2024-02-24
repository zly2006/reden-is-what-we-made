package com.github.zly2006.reden.task

import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.world.ClientWorld

val taskStack = mutableListOf<Task>()

abstract class Task(val id: String) {
    /**
     * these methods are called when state changes, dont call them directly
     */
    annotation class StateChanged

    // todo move to client & server side manager respectively
    companion object {
        private var lastTickTime = System.currentTimeMillis()
        private var lastTickedTask: Task? = null

        init {
            if (isClient) {
                ClientTickEvents.END_CLIENT_TICK.register {
                    tick()
                }
            }
        }

        fun tick() {
            val task = taskStack.lastOrNull()
            if (task != lastTickedTask && lastTickedTask != null) {
                val lastTask = lastTickedTask!!
                if (lastTask.active) {
                    lastTask.onPause()
                }
                else {
                    lastTask.onStopped()
                }
                if (task != null) {
                    if (task.active) {
                        task.onResume()
                    }
                    else {
                        task.onCreated()
                    }
                }
                lastTickedTask = task
            }
            if (task == null) return
            task.tick()
            if (!task.active) {
                taskStack.removeLast()
            }
        }
    }

    /**
     * `false` -> not ticked yet / stopped
     */
    open var active = false; protected set
    open var paused = false; protected set

    @StateChanged
    open fun onCreated() {
    }

    @StateChanged
    open fun onPause() {
        paused = true
    }

    @StateChanged
    open fun onResume() {
        paused = false
    }

    @StateChanged
    open fun onStopped() {
        paused = false
        active = false
    }

    open fun tick() {}

    /**
     * By default, enter key pressed
     */
    open fun onConfirm(): Boolean {
        return false
    }

    /**
     * By default, the escape key pressed
     */
    open fun onCancel(): Boolean {
        return false
    }

    @StateChanged
    open fun onClientSideWorldChanged(newWorld: ClientWorld?) {

    }
}
