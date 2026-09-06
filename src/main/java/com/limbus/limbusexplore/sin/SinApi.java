package com.limbus.limbusexplore.sin;

import com.limbus.limbusexplore.ego.SinCost;
import com.limbus.limbusexplore.net.PlayerSinSync;
import net.minecraft.world.entity.player.Player;

// 业务层都从这里拿/改资源，别去摸 capability 和网络包。
// 注意：改的数据是服务端的，改完自动同步；客户端读 ClientSinResources。
public final class SinApi {

    private SinApi() {
    }

    private static SinResources of(Player player) {
        return player.getCapability(ModCapabilities.SIN_RESOURCES).orElse(null);
    }

    public static int get(Player player, SinType type) {
        SinResources resources = of(player);
        return resources == null ? 0 : resources.get(type);
    }

    public static void add(Player player, SinType type, int amount) {
        SinResources resources = of(player);
        if (resources == null) {
            return;
        }
        resources.add(type, amount);
        PlayerSinSync.sync(player);
    }

    public static void set(Player player, SinType type, int amount) {
        SinResources resources = of(player);
        if (resources == null) {
            return;
        }
        resources.set(type, amount);
        PlayerSinSync.sync(player);
    }

    public static void clear(Player player) {
        SinResources resources = of(player);
        if (resources == null) {
            return;
        }
        resources.clear();
        PlayerSinSync.sync(player);
    }

    // 够扣才扣，不够返回 false
    public static boolean consume(Player player, SinType type, int amount) {
        SinResources resources = of(player);
        if (resources == null || resources.get(type) < amount) {
            return false;
        }
        resources.add(type, -amount);
        PlayerSinSync.sync(player);
        return true;
    }

    public static boolean canPay(Player player, SinCost[] costs) {
        SinResources resources = of(player);
        if (resources == null) {
            return false;
        }
        for (SinCost cost : costs) {
            if (resources.get(cost.sin()) < cost.amount()) {
                return false;
            }
        }
        return true;
    }

    // 一组消耗全够才一起扣，扣一半这种事不做
    public static boolean consumeAll(Player player, SinCost[] costs) {
        if (!canPay(player, costs)) {
            return false;
        }
        SinResources resources = of(player);
        if (resources == null) {
            return false;
        }
        for (SinCost cost : costs) {
            resources.add(cost.sin(), -cost.amount());
        }
        PlayerSinSync.sync(player);
        return true;
    }
}
