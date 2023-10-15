package com.github.zly2006.reden.debugger

var disableWatchDog = false

var tickStage: TickStage? = null

fun yieldServer() {
    tickStage?.tick()
    tickStage = tickStage?.next()
}
