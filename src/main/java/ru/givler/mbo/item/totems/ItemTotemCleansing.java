package ru.givler.mbo.item.totems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemTotemCleansing extends Item {

    /** Рефлексивное поле isBadEffect в Potion */
    private static Field badEffectField;

    static {
        try {
            badEffectField = Potion.class.getDeclaredField("isBadEffect");
            badEffectField.setAccessible(true);
        } catch (NoSuchFieldException e1) {
            try {
                badEffectField = Potion.class.getDeclaredField("field_76418_K");
                badEffectField.setAccessible(true);
            } catch (Exception e2) {
                badEffectField = null;
                e2.printStackTrace();
            }
        }
    }

    public ItemTotemCleansing(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(120);
        this.maxStackSize = maxStackSize;
        this.setFull3D();
        GameRegistry.registerItem(this, name);
    }

    private boolean isForceRemove(int potionID) {
        return potionID == Potion.invisibility.getId();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            List<PotionEffect> effectsToRemove = new ArrayList<>();

            for (Object obj : player.getActivePotionEffects()) {
                PotionEffect effect = (PotionEffect) obj;
                int potionID = effect.getPotionID();
                Potion potion = Potion.potionTypes[potionID];
                if (isBadEffect(potion) || isForceRemove(potionID)) {
                    effectsToRemove.add(effect);
                }
            }

            for (PotionEffect effect : effectsToRemove) {
                player.removePotionEffect(effect.getPotionID());
            }
            itemStack.damageItem(50, player);
        }

        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                main.proxy.spawnParticle(
                        EnumParticleType.SACRED, world,
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0
                );
            }
        }

        world.playSoundAtEntity(player, "mbo:temple", 1.0F, 1.0F);
        player.swingItem();
        return itemStack;
    }

    /** Проверяет через Reflection приватное поле isBadEffect */
    private static boolean isBadEffect(Potion potion) {
        if (badEffectField == null || potion == null) return false;
        try {
            return badEffectField.getBoolean(potion);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}
