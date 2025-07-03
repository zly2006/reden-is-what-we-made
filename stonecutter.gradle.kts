plugins {
    id("dev.kikugie.stonecutter")
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.serialization") version "2.1.21" apply false
    id("fabric-loom") version "1.10.5" apply false
    //id("dev.kikugie.j52j") version "1.0.2" apply false // Enables asset processing by writing json5 files
    id("me.modmuss50.mod-publish-plugin") version "0.7.+" apply false
}
stonecutter active "1.21.5" /* [SC] DO NOT EDIT */

stonecutter parameters {
    swaps {
        put("mod_version", "\"${property("mod.version")}\";")
    }
    dependencies {
        put("fapi", node.project.property("deps.fabric_api").toString())
    }
}
