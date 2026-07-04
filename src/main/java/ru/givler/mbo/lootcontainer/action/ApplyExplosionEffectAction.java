package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import java.util.List;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class ApplyExplosionEffectAction extends LootContainerAction {
    public String potionId = "";
    public int durationSec = 10;
    public int amplifier = 0;
    public float radius = 4.0F;
    public boolean particles = true;
    public boolean knockback = false;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_APPLY_EXPLOSION_EFFECT;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        Potion potion = LootContainerActionRuntime.resolvePotion(potionId);
        if (potion == null) return;

        int duration = Math.max(1, durationSec) * 20;
        int normalizedAmplifier = Math.max(0, amplifier);
        float normalizedRadius = Math.max(0.1F, radius);
        int centerX = tile.xCoord;
        int centerY = tile.yCoord + 1;
        int centerZ = tile.zCoord;

        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
                tile.xCoord - normalizedRadius, tile.yCoord - 2, tile.zCoord - normalizedRadius,
                tile.xCoord + normalizedRadius + 1, tile.yCoord + 3, tile.zCoord + normalizedRadius + 1
        );
        List list = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
        for (Object obj : list) {
            if (!(obj instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) obj;
            if (living.isDead) continue;

            double dx = living.posX - (tile.xCoord + 0.5D);
            double dy = living.posY - (tile.yCoord + 0.5D);
            double dz = living.posZ - (tile.zCoord + 0.5D);
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > normalizedRadius) continue;

            double intensity = 1.0D - (distance / normalizedRadius);
            int scaledDuration = Math.max(1, (int) (duration * intensity));
            living.addPotionEffect(new PotionEffect(potion.id, scaledDuration, normalizedAmplifier));
        }

        if (particles) {
            world.playAuxSFX(2002, centerX, centerY, centerZ, potion.getLiquidColor());
            LootContainerActionRuntime.playExplosionFeedback(
                    world,
                    centerX + 0.5D,
                    centerY + 0.5D,
                    centerZ + 0.5D,
                    normalizedRadius
            );
        }
        if (knockback) {
            LootContainerActionRuntime.applyExplosionKnockback(
                    world,
                    centerX + 0.5D,
                    centerY + 0.5D,
                    centerZ + 0.5D,
                    normalizedRadius
            );
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("potionId", potionId);
        json.addProperty("duration", durationSec);
        json.addProperty("amplifier", amplifier);
        json.addProperty("radius", radius);
        json.addProperty("particles", particles);
        json.addProperty("knockback", knockback);
    }

    @Override
    protected void readFields(JsonObject json) {
        potionId = readString(json, "potionId", "");
        durationSec = Math.max(1, readInt(json, "duration", readInt(json, "durationSec", 10)));
        amplifier = Math.max(0, readInt(json, "amplifier", 0));
        radius = Math.max(0.1F, readFloat(json, "radius", 4.0F));
        particles = readBoolean(json, "particles", true);
        knockback = readBoolean(json, "knockback", false);
    }
}
