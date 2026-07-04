package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class InstantDamageAction extends LootContainerAction {
    public float damage = 4.0F;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_INSTANT_DAMAGE;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        float normalizedDamage = Math.max(0.0F, damage);
        if (normalizedDamage <= 0.0F) return;

        if (source instanceof EntityLivingBase) {
            ((EntityLivingBase) source).attackEntityFrom(DamageSource.magic, normalizedDamage);
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("damage", damage);
    }

    @Override
    protected void readFields(JsonObject json) {
        damage = Math.max(0.0F, readFloat(json, "damage", 4.0F));
    }
}
