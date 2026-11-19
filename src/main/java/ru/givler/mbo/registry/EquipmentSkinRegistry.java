package ru.givler.mbo.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import minefantasy.mf2.item.weapon.ItemMaceMF;
import minefantasy.mf2.item.weapon.ItemGreatswordMF;
import minefantasy.mf2.item.weapon.ItemWaraxeMF;
import minefantasy.mf2.item.weapon.ItemWarhammerMF;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition;
import ru.givler.mbo.item.skin.EquipmentSkinDefinition.Pose;

public final class EquipmentSkinRegistry {
    public static final EquipmentSkinDefinition MACE_BASE = new EquipmentSkinDefinition("mace_base", ItemMaceMF.class,
            // MF2's held quad mirrors U: the lower-left handle is on the +X side.
            // From the grip, the standard mace points toward -X and +Y.
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0, 0.45F, 0, -15, 30, -32, 0.82F),
            new Pose(0, 0.4F, 0, 0, 20, -35, 1.0F));
    public static final EquipmentSkinDefinition HAMMER_BASE = new EquipmentSkinDefinition("hammer_base", ItemWarhammerMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 0.95F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final EquipmentSkinDefinition AXE_BASE = new EquipmentSkinDefinition("axe_base", ItemWaraxeMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 1.03F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final EquipmentSkinDefinition HAMMER = new EquipmentSkinDefinition("hammer", ItemWarhammerMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, -90, 45, 1.0F),
            new Pose(0, 0.42F, 0, -15, -62, -32, 0.95F),
            new Pose(0, 0.4F, 0, 0, -70, -35, 1.0F));
    public static final EquipmentSkinDefinition DRAGONSLAYER = new EquipmentSkinDefinition("dragonslayer", ItemGreatswordMF.class,
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(0.80F, 0.20F, -0.03125F, 0, 0, 45, 1.0F),
            new Pose(-0.14F, 0.24F, 0, -15, 25, -36, 0.64F),
            new Pose(0, 0.4F, 0, 0, 20, -35, 1.0F));
    public static final EquipmentSkinDefinition MAGICIAN_BASE = new EquipmentSkinDefinition(
            "armor_magician_base", 0b0111, new Pose(0, 0, 0, 0, 198, 0, 0.56F));
    public static final List<EquipmentSkinDefinition> ALL = Collections.unmodifiableList(
            Arrays.asList(MACE_BASE, HAMMER_BASE, AXE_BASE, HAMMER, DRAGONSLAYER, MAGICIAN_BASE));

    private EquipmentSkinRegistry() {}

    public static EquipmentSkinDefinition find(String id) {
        for (EquipmentSkinDefinition skin : ALL) if (skin.id.equals(id)) return skin;
        return null;
    }

    public static String canonicalId(String id) {
        return "mace_test".equals(id) ? MACE_BASE.id : id;
    }
}
