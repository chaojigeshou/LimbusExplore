package com.limbus.limbusexplore;

import com.limbus.limbusexplore.chaos.*;
import com.limbus.limbusexplore.combat.*;
import com.limbus.limbusexplore.config.ModConfig;
import com.limbus.limbusexplore.ego.*;
import com.limbus.limbusexplore.net.*;
import com.limbus.limbusexplore.registry.ModItems;
import com.limbus.limbusexplore.sanity.SanityApi;
import com.limbus.limbusexplore.sin.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** 在真实 Forge 注册表、事件总线和能力变换器中执行；不需要启动客户端。 */
@GameTestHolder(LimbusExplore.MODID)
@PrefixGameTestTemplate(false)
public final class IntegrationGameTests {
    private static FakePlayer player(GameTestHelper helper) {
        FakePlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "EgoTest"));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 0));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        return player;
    }

    private static void ready(FakePlayer player) {
        EgoLoadoutApi.unlock(player, Ego.EMBER_WATCH);
        EgoLoadoutApi.set(player, 0, Ego.EMBER_WATCH);
        SinApi.set(player, SinType.WRATH, 10);
        SanityApi.set(player, 45);
    }

    @GameTest(template = "empty3x3")
    public static void contractRecipeUnlocksOnlyOnce(GameTestHelper helper) {
        FakePlayer player = player(helper);
        helper.assertTrue(helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation(LimbusExplore.MODID, "ember_watch_contract")).isPresent(), "Missing recipe");
        helper.assertTrue(!EgoLoadoutApi.set(player, 0, Ego.EMBER_WATCH), "Locked EGO equipped");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMBER_WATCH_CONTRACT.get(), 2));
        ModItems.EMBER_WATCH_CONTRACT.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Contract not consumed once");
        ModItems.EMBER_WATCH_CONTRACT.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "Duplicate unlock consumed contract");
        helper.assertTrue(EgoLoadoutApi.set(player, 0, Ego.EMBER_WATCH), "Unlocked EGO rejected");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void releaseSkillAndEndCleanup(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ready(player);
        Zombie target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        target.setNoAi(true);
        ResistanceApi.set(target, DamageKind.BLUNT, 0.5f);
        float before = target.getHealth();
        helper.assertTrue(EgoApi.release(player, Ego.EMBER_WATCH) == EgoApi.ReleaseResult.RELEASED, "Release failed");
        helper.assertTrue(SinApi.get(player, SinType.WRATH) == 8 && SanityApi.get(player) == 35, "Wrong cost");
        float after = target.getHealth();
        helper.assertTrue(after < before && before - after < 8, "Typed skill failed to respect resistance");
        EgoStateApi.reconcile(player);
        helper.assertTrue(target.getHealth() == after, "Reconcile replayed one-shot skill");
        helper.assertTrue(ResistanceApi.get(player, DamageKind.BLUNT) == 0.5f, "Missing EGO resistance");
        helper.assertTrue(!EgoLoadoutApi.set(player, 0, null), "Changed equipment during EGO state");
        EgoStateApi.end(player);
        helper.assertTrue(ResistanceApi.get(player, DamageKind.BLUNT) == 1f, "Resistance leaked after end");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void skillDoesNotGoThroughWalls(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ready(player);
        Zombie target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        target.setNoAi(true);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.STONE);
        float before = target.getHealth();
        EgoApi.release(player, Ego.EMBER_WATCH);
        helper.assertTrue(target.getHealth() == before, "Skill passed through a wall");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void forgedReleaseDoesNotSpendResources(GameTestHelper helper) {
        FakePlayer player = player(helper);
        SinApi.set(player, SinType.WRATH, 10);
        SanityApi.set(player, 45);
        EgoReleaseService.handle(player, Ego.EMBER_WATCH.id, false);
        EgoReleaseService.handle(player, Ego.EMBER_WATCH.id, true);
        EgoReleaseService.handle(player, "unknown_ego", true);
        helper.assertTrue(SinApi.get(player, SinType.WRATH) == 10 && SanityApi.get(player) == 45, "Forged request spent resources");
        helper.assertTrue(!EgoStateApi.isInState(player), "Forged request entered state");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void deathClonePreservesProgressNotTemporaryState(GameTestHelper helper) {
        FakePlayer original = player(helper);
        ready(original);
        EgoApi.releaseCorrosion(original, Ego.EMBER_WATCH);
        ChaosApi.breakNow(original);
        original.invalidateCaps();
        FakePlayer copy = player(helper);
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(copy, original, true));
        helper.assertTrue(EgoLoadoutApi.isEquipped(copy, Ego.EMBER_WATCH), "Death lost unlocked equipment");
        helper.assertTrue(SinApi.get(copy, SinType.WRATH) == 7 && SanityApi.get(copy) == 30, "Death lost persistent values");
        helper.assertTrue(!EgoStateApi.isInState(copy) && !ChaosApi.isInChaos(copy), "Death copied temporary lock");
        helper.assertTrue(ResistanceApi.get(copy, DamageKind.BLUNT) == 1f, "Death copied temporary resistance");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void nonDeathCloneRestoresStateWithoutReplayingSkill(GameTestHelper helper) {
        FakePlayer original = player(helper);
        ready(original);
        EgoApi.release(original, Ego.EMBER_WATCH);
        original.invalidateCaps();
        FakePlayer copy = player(helper);
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(copy, original, false));
        EgoStateApi.reconcile(copy);
        helper.assertTrue(EgoLoadoutApi.isEquipped(copy, Ego.EMBER_WATCH), "End return lost equipment");
        helper.assertTrue(EgoStateApi.isInState(copy) && ResistanceApi.get(copy, DamageKind.BLUNT) == 0.5f, "End return lost state");
        helper.assertTrue(SinApi.get(copy, SinType.WRATH) == 8, "End return charged again");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void offlineExpiryAndReloadClearDebtAndOverride(GameTestHelper helper) {
        FakePlayer player = player(helper);
        ready(player);
        SinApi.set(player, SinType.WRATH, 0);
        EgoApi.releaseCorrosion(player, Ego.EMBER_WATCH);
        var state = player.getCapability(EgoStateCapabilities.EGO_STATE).orElseThrow(AssertionError::new);
        CompoundTag expired = state.save();
        expired.putLong("endAt", System.currentTimeMillis() - 1000);
        state.load(expired);
        EgoStateApi.reconcile(player);
        helper.assertTrue(!state.hasState() && SinApi.get(player, SinType.WRATH) == 0, "Offline expiry left state/debt");
        helper.assertTrue(ResistanceApi.get(player, DamageKind.BLUNT) == 1, "Offline expiry left resistance");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void naturalStaggerEndRunsCleanupOnce(GameTestHelper helper) {
        FakePlayer player = player(helper);
        AtomicInteger ended = new AtomicInteger();
        ChaosApi.registerListener(new ChaosListener() {
            @Override public void onEnd(net.minecraft.world.entity.LivingEntity entity, Chaos chaos) {
                if (entity == player) ended.incrementAndGet();
            }
        });
        ChaosApi.breakNow(player);
        helper.assertTrue(!EgoLoadoutApi.set(player, 0, Ego.TEST_ZAYIN), "Stagger allowed equipment mutation");
        for (int i = 0; i < Chaos.CHAOS_TICKS + 1; i++) ChaosApi.tick(player);
        helper.assertTrue(ended.get() == 1 && ChaosApi.get(player) == Chaos.MAX, "End callback/refill failed");
        helper.assertTrue(!ChaosApi.of(player).hasLock() && !ChaosApi.isInChaos(player), "Natural end left lock");
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void combatSwitchesAndImmunityAreIndependent(GameTestHelper helper) {
        FakePlayer player = player(helper);
        boolean oldKinds = ModConfig.DAMAGE_KIND_ENABLED.get();
        boolean oldChaos = ModConfig.CHAOS_ENABLED.get();
        try {
            ModConfig.DAMAGE_KIND_ENABLED.set(false);
            ModConfig.CHAOS_ENABLED.set(true);
            LivingHurtEvent hit = new LivingHurtEvent(player, player.damageSources().generic(), 10);
            CombatHandler.onLivingHurt(hit);
            helper.assertTrue(hit.getAmount() == 10 && ChaosApi.get(player) == 90, "Disabling kinds disabled stagger");
            ChaosApi.reset(player);
            ModConfig.DAMAGE_KIND_ENABLED.set(true);
            ResistanceApi.set(player, DamageKind.BLUNT, 0);
            hit = new LivingHurtEvent(player, player.damageSources().generic(), 10);
            CombatHandler.onLivingHurt(hit);
            helper.assertTrue(hit.getAmount() == 0 && ChaosApi.get(player) == 100, "Immune hit caused stagger");
            ChaosApi.breakNow(player);
            ModConfig.CHAOS_ENABLED.set(false);
            ChaosApi.tick(player);
            helper.assertTrue(!ChaosApi.of(player).hasLock() && ChaosApi.get(player) == 100, "Disabling chaos left lock");
        } finally {
            ModConfig.DAMAGE_KIND_ENABLED.set(oldKinds);
            ModConfig.CHAOS_ENABLED.set(oldChaos);
        }
        helper.succeed();
    }

    @GameTest(template = "empty3x3")
    public static void hostileKillSuppliesReleaseResources(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Zombie target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        target.setNoAi(true);
        target.hurt(player.damageSources().playerAttack(player), 1000);
        helper.assertTrue(SinApi.get(player, SinType.WRATH) == 1 && SanityApi.get(player) == 3, "No survival reward");
        target.discard();
        helper.succeed();
    }
}
