package com.github.zly2006.reden.access

import com.github.zly2006.reden.itemShadow.ItemStackOwner
import net.minecraft.item.ItemStack

@Suppress("INAPPLICABLE_JVM_NAME")
@JvmDefaultWithoutCompatibility
interface ItemStackAccess {
    fun checkStackOwners() {
        stackOwners.removeIf { this as ItemStack !in it }
    }

    @get:JvmName("getStackOwners\$reden")
    val stackOwners: MutableList<ItemStackOwner>
}
