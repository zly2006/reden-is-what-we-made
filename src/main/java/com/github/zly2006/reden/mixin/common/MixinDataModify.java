package com.github.zly2006.reden.mixin.common;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(BlockDataObject.class)
public class MixinDataModify {
    @Shadow
    @Final
    @Mutable
    public static Function<String, DataCommand.ObjectType> TYPE_FACTORY;

    @Shadow
    @Final
    private static SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        TYPE_FACTORY = argumentName -> new DataCommand.ObjectType() {

            @Override
            public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, argumentName + "Pos");
                BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(blockPos);
                if (blockEntity == null) {
                    System.out.println("BlockEntity is null" + blockPos + " " + context.getSource().getWorld().getBlockState(blockPos));
                    throw INVALID_BLOCK_EXCEPTION.create();
                }
                return new BlockDataObject(blockEntity, blockPos);
            }

            @Override
            public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
                return argument.then(CommandManager.literal("block").then(argumentAdder.apply(CommandManager.argument(argumentName + "Pos", BlockPosArgumentType.blockPos()))));
            }
        };
    }
}
