package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class SpawnEntityAction extends LootContainerAction {
    public String entityId = "";
    public int spawnCount = 1;
    public String countExpr = "1";

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_SPAWN_ENTITY;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;
        if (entityId == null || entityId.trim().isEmpty()) return;
        String expr = countExpr == null || countExpr.trim().isEmpty() ? String.valueOf(spawnCount) : countExpr;
        int normalizedSpawnCount = Math.max(1, LootContainerActionRuntime.parseCountExpression(expr, world));

        for (int i = 0; i < normalizedSpawnCount; i++) {
            Entity spawned = net.minecraft.entity.EntityList.createEntityByName(entityId, world);
            if (spawned == null) continue;
            spawned.setPosition(tile.xCoord + 0.5D, tile.yCoord + 1.0D, tile.zCoord + 0.5D);
            world.spawnEntityInWorld(spawned);
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("entityId", entityId);
        json.addProperty("spawnCount", spawnCount);
        json.addProperty("countExpr", countExpr);
    }

    @Override
    protected void readFields(JsonObject json) {
        entityId = readString(json, "entityId", "");
        int legacyCount = Math.max(1, readInt(json, "spawnCount", readInt(json, "count", 1)));
        countExpr = readString(json, "countExpr", "");
        if (countExpr == null || countExpr.trim().isEmpty()) {
            countExpr = String.valueOf(legacyCount);
        }
        spawnCount = Math.max(1, LootContainerActionRuntime.parseCountExpression(countExpr, null));
    }
}
