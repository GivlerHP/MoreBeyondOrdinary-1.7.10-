package ru.givler.mbo.integration.wood;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.block.TrapDoorBase;
import ru.givler.mbo.block.BlockBasicFence;
import ru.givler.mbo.block.BlockBasicFenceGate;
import ru.givler.mbo.block.BlockBasicWoodButton;
import ru.givler.mbo.block.BlockBasicWoodPressurePlate;
import ru.givler.mbo.item.DoorItemBase;

/** Registers one complete wood family and its recipes. */
public final class WoodFamily {
    public final DoorBase door;
    public final DoorItemBase doorItem;
    public final TrapDoorBase trapdoor;
    public final BlockBasicFence fence;
    public final BlockBasicFenceGate gate;
    public final BlockBasicWoodButton button;
    public final BlockBasicWoodPressurePlate pressurePlate;

    public WoodFamily(String id, Block planks, int meta, CreativeTabs tab) {
        String texture = "integration/" + id;
        door = new DoorBase(Material.wood, "door_" + id + "_block", texture, null);
        doorItem = new DoorItemBase(door, id + "_door_item", texture);
        door.setDropItem(doorItem);
        trapdoor = new TrapDoorBase(Material.wood, "trapdoor_" + id, texture);
        fence = new BlockBasicFence("Fence" + id, planks, meta);
        gate = new BlockBasicFenceGate("FenceGate" + id, planks, meta);
        button = new BlockBasicWoodButton("Button" + id, planks, meta);
        pressurePlate = new BlockBasicWoodPressurePlate("PressurePlate" + id, planks, meta);

        doorItem.setCreativeTab(tab);
        trapdoor.setCreativeTab(tab);
        fence.setCreativeTab(tab);
        gate.setCreativeTab(tab);
        button.setCreativeTab(tab);
        pressurePlate.setCreativeTab(tab);

        ItemStack plank = new ItemStack(planks, 1, meta);
        GameRegistry.addRecipe(new ItemStack(doorItem), "PP", "PP", "PP", 'P', plank);
        GameRegistry.addRecipe(new ItemStack(trapdoor, 2), "PPP", "PPP", 'P', plank);
        fence.addStandardRecipes();
        gate.addStandardRecipe();
        button.addStandardRecipe();
        pressurePlate.addStandardRecipe();
    }
}
