package com.limbus.limbusexplore.compat.jade;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.chaos.Chaos;
import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.combat.ResistanceApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

/** 服务端侧：把混乱值和三系抗性塞进 Jade 的数据包（会在服务端读 capability） */
public class JadeEntityData implements IServerDataProvider<EntityAccessor> {

    public static final ResourceLocation UID = new ResourceLocation(LimbusExplore.MODID, "entity_stats");

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        // 服务端侧的官方绕过入口：关掉后不再往 Jade 塞数据
        if (!com.limbus.limbusexplore.config.ModConfig.JADE_COMPAT_ENABLED.get()) {
            return;
        }
        Entity entity = accessor.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        Chaos chaos = ChaosApi.of(living);
        if (chaos != null) {
            data.putInt("chaos", chaos.get());
            data.putInt("chaosMax", chaos.getMax());
            data.putInt("chaosTicks", chaos.getChaosTicks());
        }

        for (DamageKind kind : DamageKind.values()) {
            data.putFloat("resist_" + kind.id, ResistanceApi.get(living, kind));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
