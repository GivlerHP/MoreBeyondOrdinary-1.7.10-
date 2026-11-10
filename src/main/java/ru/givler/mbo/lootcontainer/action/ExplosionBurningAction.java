package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;


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
        LootContainerActionRuntime.forEachLivingTarget(tile, normalizedRadius, living -> living.setFire(burnSeconds));
        LootContainerActionRuntime.explosionEffects(tile, normalizedRadius, particles, knockback);
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
