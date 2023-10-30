package com.github.zly2006.reden.itemShadow

import net.minecraft.block.entity.LecternBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

interface ItemStackOwner {
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
    fun checkContains(stack: ItemStack): Boolean {
        return when (type) {
            Type.Inventory -> if (this is Inventory) {
                this.containsAny { it === stack }
            } else false
            Type.Lectern -> if (this is LecternBlockEntity) {
                this.book === stack
            } else false
            Type.StackEntity -> if (this is ItemEntity) {
                this.stack === stack
            } else false
        }
    }
}
