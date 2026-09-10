package com.limbus.limbusexplore.chaos;

import com.limbus.limbusexplore.net.ChaosSyncPacket;
import com.limbus.limbusexplore.net.ModNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

/**
 * 混乱值的唯一业务入口与服务端状态机。
 * 受击扣混乱值 → 扣到 0 进入混乱（15 秒）：玩家定身、怪物停 AI；
 * 期间每秒回调和同步；结束回满混乱值。
 */
public final class ChaosApi {

    /** 混乱状态下受到的伤害倍率（图书馆式：混乱就是挨打窗口） */
    public static final float CHAOS_DAMAGE_MULTIPLIER = 1.5f;

    private static final List<ChaosListener> LISTENERS = new ArrayList<>();

    private ChaosApi() {
    }

    public static void registerListener(ChaosListener listener) {
        LISTENERS.add(listener);
    }

    public static Chaos of(LivingEntity entity) {
        return entity.getCapability(ChaosCapabilities.CHAOS).orElse(null);
    }

    public static int get(LivingEntity entity) {
        Chaos chaos = of(entity);
        return chaos == null ? 0 : chaos.get();
    }

    public static int getMax(LivingEntity entity) {
        Chaos chaos = of(entity);
        return chaos == null ? Chaos.MAX : chaos.getMax();
    }

    public static boolean isInChaos(LivingEntity entity) {
        Chaos chaos = of(entity);
        return chaos != null && chaos.isInChaos();
    }

    public static int remainingSeconds(LivingEntity entity) {
        Chaos chaos = of(entity);
        return chaos == null ? 0 : chaos.getRemainingSeconds();
    }

    public static void set(LivingEntity entity, int value) {
        Chaos chaos = of(entity);
        if (chaos == null) {
            return;
        }
        chaos.set(value);
        if (chaos.get() <= 0 && !chaos.isInChaos()) {
            enterChaos(entity, chaos);
        } else {
            sync(entity);
        }
    }

    /** 扣混乱值；扣到 0 进入混乱，返回是否进入了混乱。 */
    public static boolean damage(LivingEntity entity, int amount) {
        Chaos chaos = of(entity);
        if (chaos == null || chaos.isInChaos()) {
            return false;
        }
        if (chaos.damage(amount)) {
            enterChaos(entity, chaos);
            return true;
        }
        sync(entity);
        return false;
    }

    /** 直接打乱（技能/调试用）。 */
    public static void breakNow(LivingEntity entity) {
        Chaos chaos = of(entity);
        if (chaos == null || chaos.isInChaos()) {
            return;
        }
        chaos.set(0);
        enterChaos(entity, chaos);
    }

    /** 手动解除混乱并回满。 */
    public static void endChaos(LivingEntity entity) {
        Chaos chaos = of(entity);
        if (chaos == null || !chaos.isInChaos()) {
            return;
        }
        chaos.tickChaos();
        while (chaos.getChaosTicks() > 0) {
            chaos.tickChaos();
        }
        chaos.clearLock();
        chaos.refill();
        for (ChaosListener listener : LISTENERS) {
            listener.onEnd(entity, chaos);
        }
        sync(entity);
    }

    public static void reset(LivingEntity entity) {
        Chaos chaos = of(entity);
        if (chaos == null) {
            return;
        }
        chaos.reset();
        sync(entity);
    }

    private static void enterChaos(LivingEntity entity, Chaos chaos) {
        for (ChaosListener listener : LISTENERS) {
            listener.beforeEnter(entity, chaos);
        }
        chaos.enterChaos();
        chaos.setLock(entity.getX(), entity.getY(), entity.getZ());
        chaos.setNotifiedSecond(-1);
        // 表现：一圈粒子提示进入混乱
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    24, 0.5, 0.8, 0.5, 0.15);
        }
        sync(entity);
    }

    /**
     * 每 tick 调（服务端）：
     * 混乱中 → 定身 + 计时 + 每秒回调同步；否则 → 缓慢自然回复。
     */
    public static void tick(LivingEntity entity) {
        Chaos chaos = of(entity);
        if (chaos == null) {
            return;
        }
        if (chaos.isInChaos()) {
            holdStill(entity, chaos);
            chaos.tickChaos();
            int seconds = chaos.getRemainingSeconds();
            if (seconds != chaos.getNotifiedSecond()) {
                chaos.setNotifiedSecond(seconds);
                for (ChaosListener listener : LISTENERS) {
                    listener.whileInChaos(entity, chaos, seconds);
                }
                sync(entity);
            }
            if (chaos.getChaosTicks() <= 0) {
                endChaos(entity);
            }
            return;
        }
        if (chaos.tickRegen() && entity instanceof ServerPlayer) {
            sync(entity);   // 自然回复只给玩家发包，怪物没人看
        }
    }

    /** 混乱中的定身：位置锁回 + 清速度 + 怪物停 AI。 */
    private static void holdStill(LivingEntity entity, Chaos chaos) {
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        if (!chaos.hasLock()) {
            chaos.setLock(entity.getX(), entity.getY(), entity.getZ());
        }
        if (entity instanceof ServerPlayer player) {
            // 每 tick 拉回锁定位置：客户端怎么输入都跑不掉
            player.connection.teleport(chaos.getLockX(), chaos.getLockY(), chaos.getLockZ(),
                    player.getYRot(), player.getXRot());
        } else if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    public static void sync(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        Chaos chaos = of(entity);
        if (chaos == null) {
            return;
        }
        ModNetworking.sendToPlayer(player,
                new ChaosSyncPacket(chaos.get(), chaos.getMax(), chaos.getChaosTicks()));
    }
}
