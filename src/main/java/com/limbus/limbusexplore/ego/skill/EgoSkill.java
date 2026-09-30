package com.limbus.limbusexplore.ego.skill;

import net.minecraft.server.level.ServerPlayer;

/** 技能执行接口。不负责装备、扣费或状态机，只有成功提交释放后才调用。 */
@FunctionalInterface
public interface EgoSkill {
    void execute(ServerPlayer player);
}
