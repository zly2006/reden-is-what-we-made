package com.github.zly2006.reden.mixin;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = CommandNode.class, remap = false)
public interface IMixinCommandNode {
    @Accessor("children")
    Map<String, CommandNode<?>> children();
    @Accessor("literals")
    Map<String, LiteralCommandNode<?>> literals();
}
