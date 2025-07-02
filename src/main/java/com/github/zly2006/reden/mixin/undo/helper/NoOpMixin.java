package com.github.zly2006.reden.mixin.undo.helper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({
	net.minecraft.commands.arguments.blocks.BlockInput.class,
	net.minecraft.server.commands.CloneCommands.class,
	net.minecraft.server.commands.data.BlockDataAccessor.class,
	net.minecraft.server.network.ServerGamePacketListenerImpl.class,
	net.minecraft.world.entity.item.FallingBlockEntity.class,
	net.minecraft.world.item.BlockItem.class,
	net.minecraft.world.item.component.CustomData.class,
	net.minecraft.world.level.block.CrafterBlock.class,
	net.minecraft.world.level.block.DecoratedPotBlock.class,
	net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.class,
	net.minecraft.world.level.block.entity.BaseContainerBlockEntity.class,
	net.minecraft.world.level.block.entity.BeehiveBlockEntity.class,
	net.minecraft.world.level.block.entity.BrushableBlockEntity.class,
	net.minecraft.world.level.block.entity.CampfireBlockEntity.class,
	net.minecraft.world.level.block.entity.CommandBlockEntity.class,
	net.minecraft.world.level.block.entity.CrafterBlockEntity.class,
	net.minecraft.world.level.block.entity.JukeboxBlockEntity.class,
	net.minecraft.world.level.block.entity.LecternBlockEntity.class,
	net.minecraft.world.level.block.entity.LecternBlockEntity.class,
//		net.minecraft.world.level.block.entity.
//			SculkSensorBlockEntity.VibrationUser.class,
	net.minecraft.world.level.block.entity.SculkSensorBlockEntity.class,
	net.minecraft.world.level.block.entity.SignBlockEntity.class,
	net.minecraft.world.level.block.entity.SkullBlockEntity.class,
	net.minecraft.world.level.block.entity.SpawnerBlockEntity.class,
	net.minecraft.world.level.block.entity.StructureBlockEntity.class,
	net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity.class,
	net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity.class,
	net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.class}
)
public class NoOpMixin {
	@Unique
	boolean reden$undo$noOp = true;
}
