package com.github.zly2006.reden;

import com.github.zly2006.reden.utils.UtilsKt;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        UtilsKt.checkMalilib();
    }
}
