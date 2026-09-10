package com.limbus.limbusexplore.chaos;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class ChaosProvider implements ICapabilitySerializable<CompoundTag> {

    private final Chaos chaos = new Chaos();
    private final LazyOptional<Chaos> optional = LazyOptional.of(() -> chaos);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == ChaosCapabilities.CHAOS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return chaos.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        chaos.load(tag);
    }
}
