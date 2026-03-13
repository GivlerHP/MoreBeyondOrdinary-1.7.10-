package ru.givler.mbo.item;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;


public class ItemSilverCross extends ItemBase {
    public ItemSilverCross(String name, String texture, int maxStackSize) {
            super(name, texture, maxStackSize);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 0;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        return stack;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target) {
        if (player.worldObj.isRemote) return false;

        if (!(target instanceof EntityZombie)) return false;
        EntityZombie zombie = (EntityZombie) target;

        if (!zombie.isVillager()) return false;
        if (!zombie.isPotionActive(Potion.weakness)) return false;

        int time = zombie.worldObj.rand.nextInt(60) + 60;

        try {
            ReflectionHelper.findMethod(
                    EntityZombie.class,
                    zombie,
                    new String[]{"startConversion", "func_82231_m"},
                    int.class
            ).invoke(zombie, time);

            // Визуальный эффект конверсии
            zombie.worldObj.setEntityState(zombie, (byte) 16);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }

        return true;
    }
}