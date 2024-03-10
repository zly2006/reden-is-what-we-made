package com.github.zly2006.reden.itemShadow

import net.minecraft.block.entity.LecternBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

@Suppress("INAPPLICABLE_JVM_NAME")
@JvmDefaultWithoutCompatibility
interface ItemStackOwner {
    @get:JvmName("getType\$reden")
    val type: Type

    enum class Type {
        Inventory,
        StackEntity,
        Lectern,
    }

    /**
     * Check if the owner contains the stack.
     * This is used by item shadowing, and only check reference equality.
     */
    @JvmName("checkContains\$reden")
    operator fun contains(stack: ItemStack): Boolean {
        // Note: use === to check reference equality
        return when (type) {
            Type.Inventory -> if (this is Inventory) {
                this.containsAny { it === stack }
            } else false
            Type.Lectern -> if (this is LecternBlockEntity) {
                if (isRemoved) return false
                this.book === stack
            } else false
            Type.StackEntity -> if (this is ItemEntity) {
                if (isRemoved) return false
                this.stack === stack
            } else false
        }
    }
}
