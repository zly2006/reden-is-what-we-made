package com.github.zly2006.reden.yo

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.utils.sendMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.GameMode
import org.lwjgl.glfw.GLFW

class CheatCode(
    private val codeSequence: List<Int>,
    private val action: () -> Unit,
) {
    private var index = 0
    fun keyPressed(key: Int) {
        if (key == codeSequence[index]) {
            index++
            if (index == codeSequence.size) {
                action()
                index = 0
            }
        } else {
            index = 0
        }
    }

    companion object {
        private val cheatCodes = mutableListOf<CheatCode>()
        fun register(code: CheatCode) {
            cheatCodes.add(code)
        }
        @JvmStatic
        fun onKeyPressed(key: Int) {
            cheatCodes.forEach {
                it.keyPressed(key)
            }
        }

        init {
            register(CheatCode(listOf(
                GLFW.GLFW_KEY_UP,
                GLFW.GLFW_KEY_UP,
                GLFW.GLFW_KEY_DOWN,
                GLFW.GLFW_KEY_DOWN,
                GLFW.GLFW_KEY_LEFT,
                GLFW.GLFW_KEY_RIGHT,
                GLFW.GLFW_KEY_LEFT,
                GLFW.GLFW_KEY_RIGHT,
                GLFW.GLFW_KEY_B,
                GLFW.GLFW_KEY_A,
            )) {
                onFunctionUsed("uuddlrlrba")
                val mc = MinecraftClient.getInstance()
                if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
                    mc.networkHandler?.sendChatCommand("effect give @s regeneration 100 255")
                    val item = ItemStack(Items.DIAMOND_SWORD)
                    val level = 127
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.SHARPNESS), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.UNBREAKING), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.MENDING), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.LOOTING), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.FIRE_ASPECT), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.KNOCKBACK), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.SWEEPING_EDGE), level
                    )
                    item.addEnchantment(
                        mc.networkHandler!!.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(Enchantments.SMITE), level
                    )
                    item.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT)
                        .apply {
                            modifiers.add(
                                AttributeModifiersComponent.Entry(
                                    EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributeModifier(
                                        Reden.identifier("custom"),
                                        100.0,
                                        EntityAttributeModifier.Operation.ADD_VALUE
                                    ), AttributeModifierSlot.MAINHAND
                                )
                            )
                            modifiers.add(
                                AttributeModifiersComponent.Entry(
                                    EntityAttributes.GENERIC_MAX_HEALTH, EntityAttributeModifier(
                                        Reden.identifier("custom"),
                                        1000.0,
                                        EntityAttributeModifier.Operation.ADD_VALUE
                                    ), AttributeModifierSlot.MAINHAND
                                )
                            )
                            modifiers.add(
                                AttributeModifiersComponent.Entry(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier(
                                        Reden.identifier("custom"),
                                        10000.0,
                                        EntityAttributeModifier.Operation.ADD_VALUE
                                    ), AttributeModifierSlot.MAINHAND
                                )
                            )
                        }
                    val emptySlot = mc.player!!.inventory.emptySlot
                    mc.player!!.inventory.insertStack(emptySlot, item.copy())
                    val slotIndex = mc.player!!.playerScreenHandler.getSlotIndex(mc.player!!.inventory, emptySlot).orElse(-1)
                    mc.interactionManager?.clickCreativeStack(item.copy(), slotIndex)
                    mc.player?.sendMessage("Oops!")
                }
            })
            register(CheatCode(listOf(
                // This is Reden
                GLFW.GLFW_KEY_T,
                GLFW.GLFW_KEY_H,
                GLFW.GLFW_KEY_I,
                GLFW.GLFW_KEY_S,
                GLFW.GLFW_KEY_I,
                GLFW.GLFW_KEY_S,
                GLFW.GLFW_KEY_R,
                GLFW.GLFW_KEY_E,
                GLFW.GLFW_KEY_D,
                GLFW.GLFW_KEY_E,
                GLFW.GLFW_KEY_N,
            )) {
                onFunctionUsed("this-is-reden")
                val mc = MinecraftClient.getInstance()
                mc.player?.sendMessage("This is Reden!")
            })
        }
    }
}
