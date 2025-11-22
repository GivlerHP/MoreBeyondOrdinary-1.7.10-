package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.registry.PotionRegistry;

public class ItemVampirismAmulet extends ItemAmuletBase {

    public ItemVampirismAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(8);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        player.addPotionEffect(new PotionEffect(PotionRegistry.Vampirism.id, 100, 2));
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 45;
    }

    @Override
    public int getExperienceCost() {
        return 3;
    }
}
