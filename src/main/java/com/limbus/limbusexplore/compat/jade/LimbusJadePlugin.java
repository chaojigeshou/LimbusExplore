package com.limbus.limbusexplore.compat.jade;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade（玉）兼容入口。
 * 服务端收集混乱值 + 三系抗性写进 tooltip 数据，客户端那边读出来显示。
 * 注意：这个类只在装了 Jade 时才会被加载（Jade 扫 @WailaPlugin），
 * 所以本模组对 Jade 是可选依赖（编译期 compileOnly，运行时不强制）。
 */
@WailaPlugin(LimbusExplore.MODID)
public class LimbusJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(new JadeEntityData(), LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new JadeEntityComponent(), LivingEntity.class);
    }
}
