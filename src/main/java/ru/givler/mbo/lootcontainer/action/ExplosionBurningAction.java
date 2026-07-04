package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

import java.util.List;

public class ExplosionBurningAction extends LootContainerAction {
    public int durationSec = 5;
    public float radius = 4.0F;
    public boolean particles = true;
    public boolean knockback = false;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_EXPLOSION_BURNING;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        int burnSeconds = Math.max(1, durationSec);
        float normalizedRadius = Math.max(0.1F, radius);
        int radiusCeil = (int) Math.ceil(normalizedRadius);
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
                tile.xCoord - radiusCeil, tile.yCoord - radiusCeil, tile.zCoord - radiusCeil,
                tile.xCoord + radiusCeil + 1, tile.yCoord + radiusCeil + 1, tile.zCoord + radiusCeil + 1
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
            living.setFire(burnSeconds);
        }
        if (particles) {
            LootContainerActionRuntime.playExplosionFeedback(
                    world,
                    tile.xCoord + 0.5D,
                    tile.yCoord + 1.0D,
                    tile.zCoord + 0.5D,
                    normalizedRadius
            );
        }
        if (knockback) {
            LootContainerActionRuntime.applyExplosionKnockback(
                    world,
                    tile.xCoord + 0.5D,
                    tile.yCoord + 1.0D,
                    tile.zCoord + 0.5D,
                    normalizedRadius
            );
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("duration", durationSec);
        json.addProperty("radius", radius);
        json.addProperty("particles", particles);
        json.addProperty("knockback", knockback);
    }

    @Override
    protected void readFields(JsonObject json) {
        durationSec = Math.max(1, readInt(json, "duration", readInt(json, "durationSec", 5)));
        radius = Math.max(0.1F, readFloat(json, "radius", 4.0F));
        particles = readBoolean(json, "particles", true);
        knockback = readBoolean(json, "knockback", false);
    }
}
