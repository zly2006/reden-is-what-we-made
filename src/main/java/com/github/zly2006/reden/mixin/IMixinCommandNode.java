package com.github.zly2006.reden.mixin;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = CommandNode.class, remap = false)
public interface IMixinCommandNode {
    @Accessor("children")
    Map<String, CommandNode<ServerCommandSource>> children();
    @Accessor("literals")
    Map<String, LiteralCommandNode<ServerCommandSource>> literals();
    @Accessor("arguments")
    Map<String, ArgumentCommandNode<ServerCommandSource, ?>> arguments();
}
