package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemStaminaAmulet extends ItemAmuletBase {
    public ItemStaminaAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(10);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        if (Loader.isModLoaded("minefantasy2")) {
            modifyStamina(player, 30.0F);
        }
        player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 240, 0));
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 40;
    }

    @Override
    public int getExperienceCost() {
        return 2;
    }

    private static void modifyStamina(EntityLivingBase entity, float amount) {
        try {
            Class<?> bridge = Class.forName(
                    "ru.givler.mbo.integration.minefantasy2.MineFantasyStaminaAccess");
            bridge.getMethod("modify", EntityLivingBase.class, float.class)
                    .invoke(null, entity, amount);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access optional MineFantasy stamina API", e);
        }
    }
}
