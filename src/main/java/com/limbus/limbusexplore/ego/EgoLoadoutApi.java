package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.net.EgoLoadoutSyncPacket;
import com.limbus.limbusexplore.net.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** 服务端装备入口。测试 EGO 默认可用，正式条目必须由可信服务端解锁。 */
public final class EgoLoadoutApi {

    private EgoLoadoutApi() {
    }

    public static boolean isEquipped(ServerPlayer player, Ego ego) {
        return player.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT)
                .map(loadout -> loadout.contains(ego)).orElse(false);
    }

    /** 解锁只允许由可信服务端调用；没有 C2S 解锁包。重复解锁不消耗凭证。 */
    public static boolean unlock(ServerPlayer player, Ego ego) {
        boolean unlocked = player.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT)
                .map(loadout -> loadout.unlock(ego)).orElse(false);
        if (unlocked) sync(player);
        return unlocked;
    }

    /** null 卸下。服务端校验状态与槽位；无论接受与否都回传权威快照。 */
    public static boolean set(ServerPlayer player, int slot, Ego ego) {
        boolean changed = false;
        if (player.isAlive() && !ChaosApi.isInChaos(player) && !EgoStateApi.isInState(player)) {
            changed = player.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT)
                    .map(loadout -> loadout.set(slot, ego)).orElse(false);
        }
        sync(player);
        return changed;
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            EgoLoadout data = player.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT).orElseGet(EgoLoadout::new);
            ModNetworking.sendToPlayer(serverPlayer, new EgoLoadoutSyncPacket(data.toIds(), data.unlockedIds()));
        }
    }
}
