package ru.givler.mbo.potion;

import java.util.UUID;
import net.minecraft.entity.EntityLivingBase;

public class Fear extends MagicStatus {
    private static final String SOURCE_TAG = "mboFearedEntity";

    public Fear(int id, boolean harmful, int colour) {
        super(id, harmful, colour, "fear", 9);
    }

    public static void setSource(EntityLivingBase target, EntityLivingBase source) {
        target.getEntityData().setString(SOURCE_TAG, source.getUniqueID().toString());
    }

    public static EntityLivingBase getSource(EntityLivingBase target) {
        String value = target.getEntityData().getString(SOURCE_TAG);
        if (value.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(value);
            for (Object object : target.worldObj.loadedEntityList) {
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
