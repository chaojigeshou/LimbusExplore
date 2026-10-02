package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.ego.skill.ShockwaveSkill;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShockwaveSkillTest {
    @Test void coneExcludesRearAndOutOfRange() {
        var cone = new ShockwaveSkill(4, 8, false);
        assertTrue(cone.contains(Vec3.ZERO, new Vec3(0, 0, 1), new Vec3(0, 0, 3)));
        assertFalse(cone.contains(Vec3.ZERO, new Vec3(0, 0, 1), new Vec3(0, 0, -3)));
        assertFalse(cone.contains(Vec3.ZERO, new Vec3(0, 0, 1), new Vec3(0, 0, 5)));
    }
    @Test void corrosionIncludesRearButNotBeyondRadius() {
        var radial = new ShockwaveSkill(6, 12, true);
        assertTrue(radial.contains(Vec3.ZERO, new Vec3(0, 0, 1), new Vec3(0, 0, -5)));
        assertFalse(radial.contains(Vec3.ZERO, new Vec3(0, 0, 1), new Vec3(0, 0, 7)));
    }
}
