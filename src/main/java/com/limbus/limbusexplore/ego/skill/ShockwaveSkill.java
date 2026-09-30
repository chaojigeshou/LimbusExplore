package com.limbus.limbusexplore.ego.skill;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/** 前方扇形/周身圆形冲击。沿用原版 hurt 管线，不穿墙、不伤害玩家或友方。 */
public record ShockwaveSkill(double radius, float damage, boolean radial) implements EgoSkill {
    private static final ResourceLocation DAMAGE_ID = new ResourceLocation(LimbusExplore.MODID, "ego_blunt");

    public ShockwaveSkill {
        if (!Double.isFinite(radius) || radius <= 0 || !Float.isFinite(damage) || damage <= 0) {
            throw new IllegalArgumentException("Invalid shockwave parameters");
        }
    }

    public boolean contains(Vec3 origin, Vec3 forward, Vec3 target) {
        Vec3 offset = target.subtract(origin);
        if (offset.lengthSqr() > radius * radius) return false;
        if (radial) return true;
        Vec3 horizontal = new Vec3(offset.x, 0, offset.z);
        Vec3 facing = new Vec3(forward.x, 0, forward.z);
        return horizontal.lengthSqr() < 0.01 || horizontal.normalize().dot(facing.normalize()) >= 0.5;
    }

    @Override
    public void execute(ServerPlayer player) {
        var level = player.serverLevel();
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, DAMAGE_ID)), player);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                target -> target instanceof Enemy && target.isAlive() && !target.isAlliedTo(player))) {
            if (contains(player.position(), player.getLookAngle(), target.position())
                    && player.hasLineOfSight(target)) {
                target.hurt(source, damage);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1f, radial ? 0.65f : 1.1f);
        for (int i = 0; i < 36; i++) {
            double angle = i * Math.PI * 2 / 36;
            Vec3 point = player.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            if (contains(player.position(), player.getLookAngle(), point)) {
                level.sendParticles(radial ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        point.x, point.y + 0.8, point.z, 2, 0.1, 0.2, 0.1, 0.02);
            }
        }
    }
}
