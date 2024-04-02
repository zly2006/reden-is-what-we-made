package com.github.zly2006.reden.yo

import com.github.zly2006.reden.Reden
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import java.util.*

fun registerGreenstone() {
    val packs = listOf(
        Reden.identifier("greenstone"),
        Reden.identifier("communist_redstone")
    )
    packs.forEach {
        if (!ResourceManagerHelper.registerBuiltinResourcePack(
                it, FabricLoader.getInstance().getModContainer(Reden.MOD_ID).get(), ResourcePackActivationType.NORMAL
            )
        ) {
            Reden.LOGGER.error("Failed to register $it resource pack")
        }
    }

    if (Calendar.getInstance()[Calendar.MONTH] == Calendar.APRIL
        && Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 1
    ) {
        MinecraftClient.getInstance().resourcePackManager.enable("reden:greenstone")
    }
    if (Calendar.getInstance()[Calendar.MONTH] == Calendar.MARCH
        && Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 28
    ) {
        MinecraftClient.getInstance().resourcePackManager.enable("reden:communist_redstone")
    }
}
