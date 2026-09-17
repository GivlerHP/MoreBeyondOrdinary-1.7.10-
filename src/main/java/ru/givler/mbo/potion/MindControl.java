package ru.givler.mbo.potion;

import java.util.UUID;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class MindControl extends MagicStatus {
    private static final String CONTROLLER_TAG = "mboMagicController";

    public MindControl(int id, boolean harmful, int colour) {
        super(id, harmful, colour, "mind_control", 7);
    }

    public static void setController(EntityLivingBase target, EntityLivingBase controller) {
        target.getEntityData().setString(CONTROLLER_TAG, controller.getUniqueID().toString());
    }

    public static EntityLivingBase getController(EntityLivingBase target) {
        return findEntity(target.worldObj, target.getEntityData().getString(CONTROLLER_TAG));
    }

    private static EntityLivingBase findEntity(World world, String value) {
        if (value.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(value);
            for (Object object : world.loadedEntityList) {
                if (object instanceof EntityLivingBase
                        && uuid.equals(((EntityLivingBase) object).getUniqueID())) {
                    return (EntityLivingBase) object;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }
}
