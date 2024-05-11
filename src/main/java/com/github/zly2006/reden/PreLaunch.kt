package com.github.zly2006.reden

import com.github.zly2006.reden.utils.checkMalilib
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint

class PreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {
        checkMalilib()
        System.setProperty("org.lwjgl.util.NoChecks", "true")
    }
}
