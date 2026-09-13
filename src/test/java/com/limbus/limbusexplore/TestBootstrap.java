package com.limbus.limbusexplore;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * 少数测试要碰注册表（比如 ItemTags 里那些 tag 路径），Minecraft 要求先 bootstrap。
 * 不启动游戏、不建世界，只是把注册表建起来，多跑一次也就几十毫秒。
 */
public final class TestBootstrap {

    private static boolean done;

    private TestBootstrap() {
    }

    public static synchronized void ensure() {
        if (done) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        done = true;
    }
}
