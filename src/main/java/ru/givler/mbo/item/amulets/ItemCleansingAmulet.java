package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemCleansingAmulet extends ItemAmuletBase {

    public ItemCleansingAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(8);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        clearNegativeEffects(player);

        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setInteger("cleanse_ticks", 200);
        tag.setInteger("cleanse_cooldown", 10);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;

        NBTTagCompound tag = getOrCreateTag(stack);
        int ticksLeft = tag.getInteger("cleanse_ticks");

        if (ticksLeft > 0) {
            int cooldown = tag.getInteger("cleanse_cooldown");

            if (cooldown <= 0) {
                clearNegativeEffects((EntityPlayer) entity);
                tag.setInteger("cleanse_cooldown", 10);
            } else {
                tag.setInteger("cleanse_cooldown", cooldown - 1);
            }

            tag.setInteger("cleanse_ticks", ticksLeft - 1);
        }
    }

    private void clearNegativeEffects(EntityPlayer player) {
        player.removePotionEffect(Potion.poison.id);
        player.removePotionEffect(Potion.wither.id);
        player.removePotionEffect(Potion.blindness.id);
        player.removePotionEffect(Potion.confusion.id);
    }

    private NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 40;
    }

    @Override
    public int getExperienceCost() {
        return 3;
    }
}
