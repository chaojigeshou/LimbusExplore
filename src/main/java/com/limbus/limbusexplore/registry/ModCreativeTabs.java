package com.limbus.limbusexplore.registry;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LimbusExplore.MODID);

    public static final RegistryObject<CreativeModeTab> LIMBUS_EXPLORE_TAB = CREATIVE_MODE_TABS.register(
            "limbus_explore_tab",
            () -> CreativeModeTab.builder()
                    // 不设 title 的话标签会显示空白，translatable 键要对应 lang 里的 itemGroup.limbusexplore.*
                    .title(Component.translatable("itemGroup.limbusexplore.limbus_explore_tab"))
                    .icon(() -> ModItems.PLACEHOLDER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ModItems.PLACEHOLDER_ITEM.get()))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
