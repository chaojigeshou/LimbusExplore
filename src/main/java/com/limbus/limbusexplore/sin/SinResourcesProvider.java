package com.limbus.limbusexplore.sin;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class SinResourcesProvider implements ICapabilitySerializable<CompoundTag> {

    private final SinResources resources = new SinResources();
    private final LazyOptional<SinResources> optional = LazyOptional.of(() -> resources);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == ModCapabilities.SIN_RESOURCES ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return resources.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        resources.load(tag);
    }
}
