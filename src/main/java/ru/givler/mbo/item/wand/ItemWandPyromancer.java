package ru.givler.mbo.item.wand;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemWandBase;
import ru.givler.mbo.registry.CreativeTabRegistry;
import thaumcraft.common.entities.projectile.EntityExplosiveOrb;

import java.util.List;

public class ItemWandPyromancer extends ItemWandBase {

    private static final float STRENGTH = 1.4F;
    private static final boolean ON_FIRE = false;

    public ItemWandPyromancer(int maxDurability) {
        super(maxDurability);
        this.setUnlocalizedName("wandPyromancer");
        this.setTextureName(MoreBeyondOrdinary.MODID + ":wand/wandPyromancer");
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            EntityExplosiveOrb fireball = new EntityExplosiveOrb(world, player);
            fireball.strength = STRENGTH;
            fireball.onFire = ON_FIRE;
            world.spawnEntityInWorld(fireball);

            if (!player.capabilities.isCreativeMode) {
                itemstack.damageItem(1, player);
            }
        }

        player.swingItem();
        world.playSoundAtEntity(player, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);

        return itemstack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.wandPyromancer.desc"));
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.uncommon;
    }
}