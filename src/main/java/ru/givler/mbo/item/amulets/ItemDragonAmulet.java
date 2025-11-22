package ru.givler.mbo.item.amulets;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.item.ItemAmuletBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemDragonAmulet extends ItemAmuletBase {

    public ItemDragonAmulet(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        this.setMaxDamage(10);
        GameRegistry.registerItem(this, name);
    }


    private static final String TAG_TIMER = "DragonAmuletTimer";

    @Override
    public void activate(EntityPlayer player, ItemStack stack) {
        player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 300, 0));
        player.getEntityData().setInteger(TAG_TIMER, 300);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        int timer = player.getEntityData().getInteger(TAG_TIMER);
        if (timer > 0) {
            if (timer % 10 == 0) {
                player.extinguish();
            }
            player.getEntityData().setInteger(TAG_TIMER, timer - 1);
        }
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 40;
    }

    @Override
    public int getExperienceCost() {
        return 2;
    }
}
