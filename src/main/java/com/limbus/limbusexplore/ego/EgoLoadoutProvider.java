package com.limbus.limbusexplore.ego;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public final class EgoLoadoutProvider implements ICapabilitySerializable<CompoundTag> {

    private final EgoLoadout loadout = new EgoLoadout();
    private LazyOptional<EgoLoadout> optional = LazyOptional.of(() -> loadout);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability != EgoLoadoutCapabilities.EGO_LOADOUT) {
            return LazyOptional.empty();
        }
        // Forge reviveCaps 只恢复外层访问权限，不会恢复已失效的 LazyOptional。
        // 死亡克隆/实体恢复后首次查询时重新包装同一份数据，不能返回旧的失效引用。
        if (!optional.isPresent()) {
            optional = LazyOptional.of(() -> loadout);
        }
        return optional.cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        return loadout.save();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        loadout.load(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
