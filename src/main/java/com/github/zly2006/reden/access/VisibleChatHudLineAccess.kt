package com.github.zly2006.reden.access

import net.minecraft.text.Text

interface VisibleChatHudLineAccess {

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getText\$reden")
    @set:JvmName("setText\$reden")
    var text: Text?
}
