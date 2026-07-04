package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class ItemDropAction extends LootContainerAction {
    private static final Logger LOGGER = LogManager.getLogger("MBO.LootContainerAction.ItemDrop");

    public String itemId = "";
    public int itemMeta = 0;
    public String itemNbt = "";
    public String countExpr = "1";

    @Override
    public String getType() {
        return LootContainerActionRegistry.TYPE_ITEM_DROP;
    }

    @Override
    public void execute(TileEntityLootContainer tile, Entity source) {
        if (tile == null) return;
        World world = tile.getWorldObj();
        if (world == null || world.isRemote) return;

        Item item = (Item) Item.itemRegistry.getObject(itemId);
        if (item == null) return;
        int count = LootContainerActionRuntime.parseCountExpression(countExpr, world);
        if (count <= 0) return;

        ItemStack templateStack = new ItemStack(item, 1, Math.max(0, itemMeta));
        if (itemNbt != null && !itemNbt.trim().isEmpty()) {
            try {
                NBTBase nbt = JsonToNBT.func_150315_a(itemNbt);
                if (nbt instanceof NBTTagCompound) {
                    templateStack.setTagCompound((NBTTagCompound) nbt);
                }
            } catch (Exception ex) {
                LOGGER.error("Failed to parse itemNbt for loot action: {}", itemNbt, ex);
            }
        }
        int maxStackSize = Math.max(1, templateStack.getMaxStackSize());
        int remaining = count;
        while (remaining > 0) {
            int dropSize = Math.min(maxStackSize, remaining);
            ItemStack dropStack = templateStack.copy();
            dropStack.stackSize = dropSize;

            EntityItem entityItem = new EntityItem(
                    world,
                    tile.xCoord + 0.5D,
                    tile.yCoord + 0.5D,
                    tile.zCoord + 0.5D,
                    dropStack
            );
            entityItem.motionX = (world.rand.nextDouble() - 0.5D) * 0.1D;
            entityItem.motionY = 0.15D;
            entityItem.motionZ = (world.rand.nextDouble() - 0.5D) * 0.1D;
            world.spawnEntityInWorld(entityItem);
            remaining -= dropSize;
        }
    }

    @Override
    protected void writeFields(JsonObject json) {
        json.addProperty("itemId", itemId);
        json.addProperty("itemMeta", itemMeta);
        json.addProperty("itemNbt", itemNbt);
        json.addProperty("countExpr", countExpr);
    }

    @Override
    protected void readFields(JsonObject json) {
        itemId = readString(json, "itemId", "");
        itemMeta = Math.max(0, readInt(json, "itemMeta", 0));
        itemNbt = readString(json, "itemNbt", "");
        countExpr = readString(json, "countExpr", "1");
    }
}
