package com.limbus.limbusexplore.sanity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class SanityProvider implements ICapabilitySerializable<CompoundTag> {

    private final Sanity sanity = new Sanity();
    private final LazyOptional<Sanity> optional = LazyOptional.of(() -> sanity);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == SanityCapabilities.SANITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return sanity.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        sanity.load(tag);
    }
}
