package com.limbus.limbusexplore.item;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoLoadoutApi;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 可合成的解锁凭证：由服务端消费，重复使用不吞物品，也不凭空给予释放资源。 */
public final class EgoContractItem extends Item {
    private final Ego ego;

    public EgoContractItem(Ego ego, Properties properties) {
        super(properties);
        this.ego = ego;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (!EgoLoadoutApi.unlock(serverPlayer, ego)) {
                player.displayClientMessage(Component.translatable("ego.limbusexplore.already_unlocked"), true);
                return InteractionResultHolder.fail(stack);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.displayClientMessage(Component.translatable("ego.limbusexplore.unlocked",
                    Component.translatable(ego.displayKey())), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
