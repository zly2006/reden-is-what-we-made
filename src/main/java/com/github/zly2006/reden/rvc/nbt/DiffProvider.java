package com.github.zly2006.reden.rvc.nbt;

import net.minecraft.nbt.NbtCompound;

public interface DiffProvider {
    NbtDiff get(NbtCompound before, NbtCompound after);
}
