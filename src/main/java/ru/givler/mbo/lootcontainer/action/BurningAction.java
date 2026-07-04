package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class BurningAction extends LootContainerAction {
    public int durationSec = 5;

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_BURNING;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        int burnSeconds = Math.max(1, durationSec);
        if (source instanceof EntityLivingBase) {
            ((EntityLivingBase) source).setFire(burnSeconds);
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("duration", durationSec);
    }

    @Override
    protected void readFields(JsonObject json) {
        durationSec = Math.max(1, readInt(json, "duration", readInt(json, "durationSec", 5)));
    }
}
