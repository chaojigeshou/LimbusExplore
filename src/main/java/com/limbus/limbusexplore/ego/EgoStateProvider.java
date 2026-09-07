package com.limbus.limbusexplore.ego;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class EgoStateProvider implements ICapabilitySerializable<CompoundTag> {

    private final EgoState state = new EgoState();
    private final LazyOptional<EgoState> optional = LazyOptional.of(() -> state);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        return capability == EgoStateCapabilities.EGO_STATE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return state.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        state.load(tag);
    }
}
