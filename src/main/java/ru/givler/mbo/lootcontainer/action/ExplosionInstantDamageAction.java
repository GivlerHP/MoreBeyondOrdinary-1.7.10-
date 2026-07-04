package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

import java.util.List;

public class ExplosionInstantDamageAction extends LootContainerAction {
    public float damage = 4.0F;
    public float radius = 4.0F;
    public boolean particles = true;
    public boolean knockback = false;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_EXPLOSION_INSTANT_DAMAGE;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        float normalizedDamage = Math.max(0.0F, damage);
        float normalizedRadius = Math.max(0.1F, radius);
        if (normalizedDamage <= 0.0F) return;

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
            living.attackEntityFrom(DamageSource.magic, normalizedDamage);
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
        json.addProperty("damage", damage);
        json.addProperty("radius", radius);
        json.addProperty("particles", particles);
        json.addProperty("knockback", knockback);
    }

    @Override
    protected void readFields(JsonObject json) {
        damage = Math.max(0.0F, readFloat(json, "damage", 4.0F));
        radius = Math.max(0.1F, readFloat(json, "radius", 4.0F));
        particles = readBoolean(json, "particles", true);
        knockback = readBoolean(json, "knockback", false);
    }
}
