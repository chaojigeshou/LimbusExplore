package com.limbus.limbusexplore.sanity;

import com.limbus.limbusexplore.net.PlayerSanitySync;
import net.minecraft.world.entity.player.Player;

// 理智值的业务入口，和 SinApi 一个套路：改完自动同步，别碰 capability 和网络层。
// reset 是战斗开始时的钩子，等战斗系统来调。
public final class SanityApi {

    private SanityApi() {
    }

    private static Sanity of(Player player) {
        return player.getCapability(SanityCapabilities.SANITY).orElse(null);
    }

    public static int get(Player player) {
        Sanity sanity = of(player);
        return sanity == null ? 0 : sanity.get();
    }

    public static void add(Player player, int amount) {
        Sanity sanity = of(player);
        if (sanity == null) {
            return;
        }
        sanity.add(amount);
        PlayerSanitySync.sync(player);
    }

    public static void set(Player player, int value) {
        Sanity sanity = of(player);
        if (sanity == null) {
            return;
        }
        sanity.set(value);
        PlayerSanitySync.sync(player);
    }

    public static void reset(Player player) {
        Sanity sanity = of(player);
        if (sanity == null) {
            return;
        }
        sanity.reset();
        PlayerSanitySync.sync(player);
    }

    public static boolean canPay(Player player, int amount) {
        Sanity sanity = of(player);
        return sanity != null && sanity.canConsume(amount);
    }

    public static boolean consume(Player player, int amount) {
        Sanity sanity = of(player);
        if (sanity == null || !sanity.consume(amount)) {
            return false;
        }
        PlayerSanitySync.sync(player);
        return true;
    }

    /** 不查下限直接扣（侵蚀释放），扣完收缩到 -45。 */
    public static void consumeForce(Player player, int amount) {
        Sanity sanity = of(player);
        if (sanity == null) {
            return;
        }
        sanity.consumeForce(amount);
        PlayerSanitySync.sync(player);
    }

    // 到 -45 就算崩溃，战斗系统判断侵蚀触发用它
    public static boolean isInChaos(Player player) {
        return get(player) <= Sanity.MIN;
    }
}
