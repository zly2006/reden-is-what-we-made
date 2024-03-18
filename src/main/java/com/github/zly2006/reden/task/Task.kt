package com.github.zly2006.reden.task

import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import org.slf4j.LoggerFactory

val taskStack = mutableListOf<Task>()
private var nextId = 45510

abstract class Task(val name: String) {
    val id: Int = nextId++
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

        inline fun <reified T : Task> all(): List<T> {
            return taskStack.filterIsInstance<T>()
        }

        private fun tick() {
            val consumer: (String) -> Unit = { MinecraftClient.getInstance().player?.sendMessage(it) }
            val task = taskStack.lastOrNull()
            if (task != lastTickedTask) {
                if (lastTickedTask != null) {
                    val lastTask = lastTickedTask!!
                    if (lastTask.active) {
                        lastTask.onPause()
                        LOGGER.debug("{} paused", lastTask)
                        consumer("$lastTask paused")
                    }
                    else {
                        lastTask.onStopped()
                        LOGGER.debug("{} stopped", lastTask)
                        consumer("$lastTask stopped")
                    }
                }
                if (task != null) {
                    if (task.active) {
                        task.onResume()
                        LOGGER.debug("{} resumed", task)
                        consumer("$task resumed")
                    }
                    else {
                        task.onCreated()
                        LOGGER.debug("{} created", task)
                        consumer("$task created")
                    }
                }
                lastTickedTask = task
            }
            if (task == null) return
            task.tick()
            if (!task.active) {
                taskStack.removeLast()
                LOGGER.debug("{} is not active, removed from stack", task)
                consumer("$task is not active, removed from stack")
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
        active = false
        return false
    }

    @StateChanged
    open fun onClientSideWorldChanged(newWorld: ClientWorld?) {
        onCancel()
    }

    override fun toString(): String {
        return "Task#$id $name"
    }
}
