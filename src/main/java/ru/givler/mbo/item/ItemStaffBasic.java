package ru.givler.mbo.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumNpcToolMaterial;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.items.ItemStaff;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.ItemRegistry.*;

public class ItemStaffBasic extends ItemStaff {

    @Override
    public void renderSpecial() {
        if (this == ItemRegistry.BrokenStaffFire) {
        GL11.glScalef(0.8f, 0.9f, 0.8f);
        GL11.glTranslatef(0.1f, -0.2f, 0.05f);
        } else if (this == ItemRegistry.BrokenGrimoireWater) {
            GL11.glScalef(0.7f, 0.8f, 0.7f);
            GL11.glTranslatef(0.1f, -0.15f, 0.05f);
        } else {
            super.renderSpecial();
        }
    }

    public ItemStaffBasic(int id, EnumNpcToolMaterial material) {
        super(id, material);
        setCreativeTab(CustomItems.tabWeapon);
    }

    @Override
    public ItemStack getProjectile(ItemStack stack) {

        if (stack.getItem() == CustomItems.staffWood) {
            return new ItemStack(CustomItems.spellNature);
        }
        if (stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic) {
            return new ItemStack(CustomItems.spellDark);
        }
        if (stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril) {
            return new ItemStack(CustomItems.spellHoly);
        }
        if (stack.getItem() == CustomItems.staffBronze) {
            return new ItemStack(CustomItems.spellLightning);
        }
        if (stack.getItem() == CustomItems.staffGold || stack.getItem() == ItemRegistry.BrokenStaffFire) {
            return new ItemStack(CustomItems.spellFire);
        }
        if (stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost) {
            return new ItemStack(CustomItems.spellIce);
        }
        if (stack.getItem() == CustomItems.staffEmerald) {
            return new ItemStack(CustomItems.spellArcane);
        }
        if (stack.getItem() == ItemRegistry.BrokenGrimoireWater) {
            return new ItemStack(CustomItems.waterElement);
        }

        return new ItemStack(CustomItems.orb, 1, stack.getItemDamage());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldObj, EntityPlayer player, int par4) {
        if (worldObj.isRemote) {
            return;
        }
        if (stack.stackTagCompound == null)
            return;
        Entity entity = ((WorldServer) player.worldObj).getEntityByID(stack.stackTagCompound.getInteger("MagicProjectile"));
        if (entity == null || !(entity instanceof EntityProjectile))
            return;
        EntityProjectile item = (EntityProjectile) entity;
        item.callback = this;
        item.callbackItem = stack;
        item.explosive = false;
        item.explosiveDamage = false;
        item.explosiveRadius = 1;
        item.prevRotationYaw = item.rotationYaw = player.rotationYaw;
        item.prevRotationPitch = item.rotationPitch = player.rotationPitch;
        item.shoot(2);

        player.worldObj.playSoundAtEntity(player, "customnpcs:magic.shot", 1.0F, 1);

        if (!player.capabilities.isCreativeMode) {
            stack.damageItem(1, player);
        }
    }

    @Override
    public void spawnParticle(ItemStack stack, EntityPlayer player) {
        if (stack.getItem() == CustomItems.staffWood) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 5, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 12, 2);
        } else if (stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x563357, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x432744, 2);
        } else if (stack.getItem() == CustomItems.staffBronze) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x83F7F6, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x5CF0FF, 2);
        } else if (stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFCFFC9, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xEFFF97, 2);
        } else if (stack.getItem() == CustomItems.staffGold ||
                stack.getItem() == ItemRegistry.BrokenStaffFire) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 1, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 14, 2);
        } else if (stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x94DFED, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x44B6FF, 2);
        } else if (stack.getItem() == CustomItems.staffEmerald) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFFC3E7, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFB92FF, 2);
        }   else if (stack.getItem() == ItemRegistry.BrokenGrimoireWater) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x3FA7D6, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x1C75BC, 2);
        }
    }
}