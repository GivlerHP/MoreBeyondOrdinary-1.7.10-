package ru.givler.mbo.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import minefantasy.mf2.item.weapon.ItemMaceMF;
import minefantasy.mf2.item.weapon.ItemGreatswordMF;
import minefantasy.mf2.item.weapon.ItemWaraxeMF;
import minefantasy.mf2.item.weapon.ItemWarhammerMF;
import ru.givler.mbo.item.skin.WeaponSkinDefinition;
import ru.givler.mbo.item.skin.WeaponSkinDefinition.Pose;

public final class WeaponSkinRegistry {
    public static final WeaponSkinDefinition MACE_BASE = new WeaponSkinDefinition("mace_base", ItemMaceMF.class,
            // MF2's held quad mirrors U: the lower-left handle is on the +X side.
            // From the grip, the standard mace points toward -X and +Y.
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0, 0.45F, 0, -15, 30, -32, 0.82F),
            new Pose(0, 0.4F, 0, 0, 20, -35, 1.0F));
    public static final WeaponSkinDefinition HAMMER_BASE = new WeaponSkinDefinition("hammer_base", ItemWarhammerMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 0.95F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final WeaponSkinDefinition AXE_BASE = new WeaponSkinDefinition("axe_base", ItemWaraxeMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 1.03F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final WeaponSkinDefinition HAMMER = new WeaponSkinDefinition("hammer", ItemWarhammerMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 0.95F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final WeaponSkinDefinition DRAGONSLAYER = new WeaponSkinDefinition("dragonslayer", ItemGreatswordMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(-0.14F, 0.24F, 0, -15, 25, -36, 0.64F),
            new Pose(0, 0.4F, 0, 0, 20, -35, 1.0F));
    public static final List<WeaponSkinDefinition> ALL = Collections.unmodifiableList(
            Arrays.asList(MACE_BASE, HAMMER_BASE, AXE_BASE, HAMMER, DRAGONSLAYER));

    private WeaponSkinRegistry() {}

    public static WeaponSkinDefinition find(String id) {
        for (WeaponSkinDefinition skin : ALL) if (skin.id.equals(id)) return skin;
        return null;
    }

    public static String canonicalId(String id) {
        return "mace_test".equals(id) ? MACE_BASE.id : id;
    }
}
