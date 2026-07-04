package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class ApplyEffectAction extends LootContainerAction {
    public String potionId = "";
    public int durationSec = 10;
    public int amplifier = 0;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_APPLY_EFFECT;
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

        if (source instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) source;
            if (!living.isDead) {
                living.addPotionEffect(new PotionEffect(potion.id, duration, normalizedAmplifier));
            }
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("potionId", potionId);
        json.addProperty("duration", durationSec);
        json.addProperty("amplifier", amplifier);
    }

    @Override
    protected void readFields(JsonObject json) {
        potionId = readString(json, "potionId", "");
        durationSec = Math.max(1, readInt(json, "duration", readInt(json, "durationSec", 10)));
        amplifier = Math.max(0, readInt(json, "amplifier", 0));
    }
}
