package ru.givler.mbo.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import minefantasy.mf2.item.weapon.ItemMaceMF;
import ru.givler.mbo.item.skin.WeaponSkinDefinition;
import ru.givler.mbo.item.skin.WeaponSkinDefinition.Pose;

public final class WeaponSkinRegistry {
    public static final WeaponSkinDefinition MACE_TEST = new WeaponSkinDefinition("mace_test", ItemMaceMF.class,
            // MF2's held quad mirrors U: the lower-left handle is on the +X side.
            // From the grip, the standard mace points toward -X and +Y.
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 0.72F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 0.72F),
            new Pose(0, 0.45F, 0, -15, 30, -32, 0.82F),
            new Pose(0, 0.4F, 0, 0, 20, -35, 0.65F));
    public static final List<WeaponSkinDefinition> ALL = Collections.unmodifiableList(Arrays.asList(MACE_TEST));

    private WeaponSkinRegistry() {}

    public static WeaponSkinDefinition find(String id) {
        for (WeaponSkinDefinition skin : ALL) if (skin.id.equals(id)) return skin;
        return null;
    }
}
