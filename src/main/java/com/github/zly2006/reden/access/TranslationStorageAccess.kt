package com.github.zly2006.reden.access

import net.minecraft.text.Text

interface TranslationStorageAccess {

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTextMap\$reden")
    val textMap: Map<String, Text>
}
