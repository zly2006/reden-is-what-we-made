package com.github.zly2006.reden.task

import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import org.slf4j.LoggerFactory

val taskStack = mutableListOf<Task>()

abstract class Task(val id: String) {
    /**
     * these methods are called when state changes, don't call them directly
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class StateChanged

    // todo move to client & server side manager respectively
    companion object {
        private val LOGGER = LoggerFactory.getLogger("Reden/Task Manager")
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
            val consumer: (String) -> Unit = { MinecraftClient.getInstance().player?.sendMessage(it) }
            val task = taskStack.lastOrNull()
            if (task != lastTickedTask) {
                if (lastTickedTask != null) {
                    val lastTask = lastTickedTask!!
                    if (lastTask.active) {
                        lastTask.onPause()
                        LOGGER.debug("Task ${lastTask.id} paused")
                        consumer("Task ${lastTask.id} paused")
                    }
                    else {
                        lastTask.onStopped()
                        LOGGER.debug("Task ${lastTask.id} stopped")
                        consumer("Task ${lastTask.id} stopped")
                    }
                }
                if (task != null) {
                    if (task.active) {
                        task.onResume()
                        LOGGER.debug("Task ${task.id} resumed")
                        consumer("Task ${task.id} resumed")
                    }
                    else {
                        task.onCreated()
                        LOGGER.debug("Task ${task.id} created")
                        consumer("Task ${task.id} created")
                    }
                }
                lastTickedTask = task
            }
            if (task == null) return
            task.tick()
            if (!task.active) {
                taskStack.removeLast()
                LOGGER.debug("Task ${task.id} is not active, removed from stack")
                consumer("Task ${task.id} is not active, removed from stack")
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
        active = true
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
