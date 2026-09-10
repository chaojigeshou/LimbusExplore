package com.limbus.limbusexplore.combat;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class ResistanceProvider implements ICapabilitySerializable<CompoundTag> {

    private final Resistance resistance = new Resistance();
    private final LazyOptional<Resistance> optional = LazyOptional.of(() -> resistance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == ResistanceCapabilities.RESISTANCE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return resistance.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        resistance.load(tag);
    }
}
