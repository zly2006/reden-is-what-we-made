package com.github.zly2006.reden.mixin.debug;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import static net.minecraft.block.ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES;

@Mixin(ChiseledBookshelfBlock.class)
public abstract class MixinChiseledBookshelf extends BlockWithEntity {
    protected MixinChiseledBookshelf(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (world1.isClient || !FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return;
            }
            var e = (ChiseledBookshelfBlockEntity) blockEntity;
            for (int i = 0; i < 6; i++) {
                Boolean b1 = state1.get(SLOT_OCCUPIED_PROPERTIES.get(i));
                Boolean b2 = !e.getStack(i).isEmpty();
                if (b1 != b2) {
                    world1.getServer().getPlayerManager().broadcast(
                            Text.literal("Slot " + i + " is " + (b2 ? "occupied" : "empty") + " at " + pos.toShortString()),
                            false
                    );
                }
            }
        };
    }
}
