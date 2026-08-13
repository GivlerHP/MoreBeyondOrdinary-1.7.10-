package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.registry.PotionRegistry;

public class ItemGoblinAmulet extends ItemAmuletBase {
    public ItemGoblinAmulet(String name, String texture) {
        setUnlocalizedName(name);
        setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setMaxDamage(70);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        player.addPotionEffect(new PotionEffect(PotionRegistry.Looting.id, 20 * 8, 0));
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 20;
    }

    @Override
    public int getExperienceCost() {
        return 1;
    }
}
