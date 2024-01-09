package com.github.zly2006.reden.mixin.sctuctureBlock;

import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StructureBlockBlockEntity.class)
public class MixinStructureBlockEntity {
    @ModifyConstant(
            method = "detectStructureSize",
            constant = @Constant(intValue = 80)
    )
    private int modifyStructureBlockDetectRange(int constant) {
        if (RedenCarpetSettings.Options.modifyStructureBlockDetectRange == -1) {
            return constant;
        } else {
            return RedenCarpetSettings.Options.modifyStructureBlockDetectRange;
        }
    }
}
