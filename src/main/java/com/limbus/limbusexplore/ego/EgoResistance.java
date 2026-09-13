package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.combat.ResistanceApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * EGO 状态的抗性覆盖：进状态把那一系抗性换成 EGO 的倍率，结束还原。
 * 走抵抗性的覆盖层，不动数据包 tag 给的基础值，所以不用存档旧值。
 * 放在 ego 包里是为了保持单向依赖（ego → combat），放 combat 里就跟 Ego 成环了。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EgoResistance implements EgoStateListener {

    private static final EgoResistance INSTANCE = new EgoResistance();

    private EgoResistance() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        EgoStateApi.registerListener(INSTANCE);
    }

    @Override
    public void beforeEnter(ServerPlayer player, Ego ego, boolean corroded) {
        apply(player, ego);
    }

    // 每秒再对一次：中途读档进来的也能补上覆盖
    @Override
    public void whileInState(ServerPlayer player, Ego ego, boolean corroded, int remainingSeconds) {
        apply(player, ego);
    }

    @Override
    public void onEnd(ServerPlayer player, Ego ego, boolean corroded) {
        ResistanceApi.clearOverride(player);
    }

    private static void apply(ServerPlayer player, Ego ego) {
        if (ego == null) {
            return;
        }
        ResistanceApi.setOverride(player, ego.resistanceKind, ego.resistanceRate);
    }
}
