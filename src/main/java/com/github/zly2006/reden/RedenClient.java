package com.github.zly2006.reden;

import com.github.zly2006.reden.report.ReportKt;
import net.fabricmc.api.ClientModInitializer;

public class RedenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReportKt.initReport();
    }
}
