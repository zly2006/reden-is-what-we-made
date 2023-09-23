package com.redenmc.mr120

import com.redenmc.mineroutine.Frame


enum class TickStage120 (

) {
    /**
     * Player tick stage. aka. Network Update, NU
     */
    PLAYERS {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Player tick stage, but dont tick/modify other things.
     *
     * Thus, pressure plate etc. will not be triggered by players.
     */
    PLAYERS_ONLY {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * [net.minecraft.world.border.WorldBorder.tick]
     */
    WORLD_BORDER {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * [net.minecraft.server.world.ServerWorld.tickWeather]
     */
    WEATHER {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Tick sleeping status
     */
    SLEEPING {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Update world time
     */
    TIME {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Block scheduled ticks
     */
    BLOCK_SCHEDULED_TICKS {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Fluid scheduled ticks
     */
    FLUID_SCHEDULED_TICKS {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Raids
     */
    RAID {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Spawn mobs, random ticks, lighting bolt
     */
    SPAWN_AND_TICK {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * [net.minecraft.server.world.ServerWorld.tickSpawners]
     */
    CUSTOM_SPAWNER {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    /**
     * Block events
     */
    BLOCK_EVENTS {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    ENTITIES {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },

    BLOCK_ENTITIES {
        override fun tick(): Frame {
            TODO("Not yet implemented")
        }
    },
    ;

    abstract fun tick(): Frame

    companion object {

        val STAGES_1_19 = listOf(
            WORLD_BORDER,
            WEATHER,
            SLEEPING,
            TIME,
            BLOCK_SCHEDULED_TICKS,
            FLUID_SCHEDULED_TICKS,
            RAID,
            SPAWN_AND_TICK,
            CUSTOM_SPAWNER,
            BLOCK_EVENTS,
            ENTITIES,
            BLOCK_ENTITIES,
        )
    }
}
