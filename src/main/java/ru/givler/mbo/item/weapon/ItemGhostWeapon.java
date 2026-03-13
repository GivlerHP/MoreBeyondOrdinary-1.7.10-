package ru.givler.mbo.item.weapon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemGhostWeapon extends ItemWeaponBase {


    public ItemGhostWeapon(String name, String texture, ToolMaterial material, int maxDamage, int maxStackSize, float scale) {
        super(name, texture, material, maxDamage, maxStackSize, scale);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            tickDurability(stack, world, player);
        }
    }

    protected void tickDurability(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return;

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        long lastTimeChecked = stack.getTagCompound().getLong("lastTimeChecked");
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - lastTimeChecked >= 1000) {
            stack.getTagCompound().setLong("lastTimeChecked", currentTimeMillis);

            if (stack.getItemDamage() < stack.getMaxDamage()) {
                stack.setItemDamage(stack.getItemDamage() + 1);
            }

            if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
                if (player.getHeldItem() == stack) {
                    player.setCurrentItemOrArmor(0, null);
                }
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    if (player.inventory.getStackInSlot(i) == stack) {
                        player.inventory.setInventorySlotContents(i, null);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
        return false;
    }
}
