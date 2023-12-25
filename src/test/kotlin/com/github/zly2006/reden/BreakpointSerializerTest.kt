package com.github.zly2006.reden

import com.github.zly2006.reden.debugger.breakpoint.BlockUpdatedBreakpoint
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.breakpoint.behavior.FreezeGame
import kotlinx.serialization.json.Json
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BreakpointSerializerTest {
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            SharedConstants.createGameVersion()
            Bootstrap.initialize()
        }
    }

    @Test
    fun test1() {
        val manager = BreakpointsManager(false)
        BreakpointsManager.testBreakpointManager = manager
        val breakpoint = BlockUpdatedBreakpoint.create(0).apply {
            name = "A breakpoint"
            world = World.OVERWORLD.value
            handler.add(BreakPoint.Handler(FreezeGame(), name = "Test Handler"))
        }
        manager.breakpointMap[0] = breakpoint

        val jsonString = json.encodeToString(manager.breakpointSerializer(), breakpoint)
        println(jsonString)
        val deserialized = json.decodeFromString(manager.breakpointSerializer(), jsonString)

        assert(breakpoint.name == deserialized.name)
        assert(breakpoint.type == deserialized.type)
        assert(breakpoint.flags == deserialized.flags)
        assert(breakpoint.world == breakpoint.world)
        assert(breakpoint.options == breakpoint.options)
        assert(breakpoint.handler == breakpoint.handler)
    }
}