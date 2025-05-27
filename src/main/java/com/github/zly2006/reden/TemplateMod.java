package com.github.zly2006.reden;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("template");
    public static final String VERSION = /*$ mod_version*/ "0.1.0";

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        //? if !release
        LOGGER.warn("I'm still a template!");

        //? if fapi: <0.95 {
        LOGGER.info("Fabric API is old on this version");
        LOGGER.info("Please update!");
        //?}
    }
}
