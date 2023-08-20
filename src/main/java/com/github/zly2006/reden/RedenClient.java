package com.github.zly2006.reden;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.report.ReportKt;
import com.github.zly2006.reden.utils.DebugKt;
import kotlin.Unit;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class RedenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReportKt.initReport();
        DebugKt.debugLogger = str -> {
            if (MinecraftClient.getInstance().player != null && MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue()) {
                MinecraftClient.getInstance().player.sendMessage(Text.of(str));
            }
            return Unit.INSTANCE;
        };
    }
}
