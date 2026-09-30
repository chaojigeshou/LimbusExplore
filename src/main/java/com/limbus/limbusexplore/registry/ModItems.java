package com.limbus.limbusexplore.registry;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.item.EgoContractItem;

// 所有物品注册都写在这个类里
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LimbusExplore.MODID);

    // 示例物品，新物品照这个写。要显示在创造模式还得去 ModCreativeTabs 里 accept，再补 lang
    public static final RegistryObject<Item> PLACEHOLDER_ITEM = ITEMS.register(
            "placeholder_item",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMBER_WATCH_CONTRACT = ITEMS.register(
            "ember_watch_contract", () -> new EgoContractItem(Ego.EMBER_WATCH, new Item.Properties().stacksTo(16)));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
