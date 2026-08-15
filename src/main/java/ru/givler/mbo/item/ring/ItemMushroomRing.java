package ru.givler.mbo.item.ring;

/**
 * Marker type for the mushroom ring's special gameplay effect.
 * Its ordinary health bonus is provided by the shared ItemStatRing logic.
 */
public class ItemMushroomRing extends ItemStatRing {
    public ItemMushroomRing(String name, String texture, double healthBonus, String descriptionLevel) {
        super(name, texture, Stat.HEALTH, healthBonus, descriptionLevel);
    }
}
