package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemGlyphBasic extends Item {
    public ItemGlyphBasic(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":glyph/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(360);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase player, EntityLivingBase Entity) {
        par1ItemStack.damageItem(1, Entity);
        return false;
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        return itemStack;
    }

    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player, EntityLivingBase Entity) {
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.getCurrentEquippedItem() == stack) {
                tickDurability(stack, world, player);
            }
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
                player.setCurrentItemOrArmor(0, null);
            }
        }
    }


    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
    }

    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }
}
