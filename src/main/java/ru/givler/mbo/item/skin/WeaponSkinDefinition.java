package ru.givler.mbo.item.skin;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** Shared catalog entry. Pose values are in item render units and degrees. */
public final class WeaponSkinDefinition {
    public final String id;
    public final Class<? extends Item> weaponType;
    public final ResourceLocation model, texture;
    public final Pose firstPerson, thirdPerson, inventory, ground;

    public WeaponSkinDefinition(String id, Class<? extends Item> weaponType,
                                Pose firstPerson, Pose thirdPerson, Pose inventory, Pose ground) {
        this.id = id;
        this.weaponType = weaponType;
        this.firstPerson = firstPerson;
        this.thirdPerson = thirdPerson;
        this.inventory = inventory;
        this.ground = ground;
        model = new ResourceLocation("mbo", "geo/weapon/" + id + ".geo.json");
        texture = new ResourceLocation("mbo", "textures/models/weapon/" + id + ".png");
    }

    public boolean supports(Item item) {
        return weaponType.isInstance(item);
    }

    public String titleKey() {
        return "mbo.weapon_skin." + id;
    }

    public static final class Pose {
        public final float x, y, z, rotateX, rotateY, rotateZ, scale;
        public Pose(float x, float y, float z, float rotateX, float rotateY, float rotateZ, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotateX = rotateX;
            this.rotateY = rotateY;
            this.rotateZ = rotateZ;
            this.scale = scale;
        }
    }
}
