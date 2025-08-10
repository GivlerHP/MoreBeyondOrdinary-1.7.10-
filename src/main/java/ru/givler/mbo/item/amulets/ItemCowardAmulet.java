package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.registry.PotionRegistry;

public class ItemCowardAmulet extends ItemAmuletBase {

    public ItemCowardAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(1);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        player.addPotionEffect(new PotionEffect(Potion.invisibility.id, 600, 0));
        player.addPotionEffect(new PotionEffect(Potion.resistance.id, 160, 4));
        player.addPotionEffect(new PotionEffect(PotionRegistry.Disarm.id, 600, 0));
        player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 200, 0));
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 180;
    }

    @Override
    public int getExperienceCost() {
        return 5;
    }
}
