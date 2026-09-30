package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.ego.skill.EgoSkill;
import com.limbus.limbusexplore.ego.skill.ShockwaveSkill;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** 行为目录与 Ego 元数据分离。加入新行为不需要修改释放事务、装备或网络处理。 */
public final class EgoSkills {
    private record Forms(EgoSkill awakening, EgoSkill corrosion) {}
    private static final Map<String, Forms> SKILLS = Map.of(
            Ego.EMBER_WATCH.id, new Forms(new ShockwaveSkill(4, 8, false), new ShockwaveSkill(6, 12, true)));

    private EgoSkills() {}

    public static void execute(ServerPlayer player, Ego ego, boolean corroded) {
        Forms forms = SKILLS.get(ego.id);
        // 旧测试 EGO 保留仅状态/抗性覆盖的行为，不偷偷赋予新伤害技能。
        if (forms != null) (corroded ? forms.corrosion : forms.awakening).execute(player);
    }
}
