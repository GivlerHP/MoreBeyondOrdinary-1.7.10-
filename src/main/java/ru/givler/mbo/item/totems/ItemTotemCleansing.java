package ru.givler.mbo.item.totems;

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

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        // Убираем все эффекты, кроме положительных
        if (!world.isRemote) {
            List<PotionEffect> effectsToRemove = new ArrayList<>();

            for (Object obj : player.getActivePotionEffects()) { // Приводим Object к PotionEffect
                PotionEffect effect = (PotionEffect) obj;
                int potionID = effect.getPotionID();
                if (potionID != Potion.moveSpeed.id && potionID != Potion.digSpeed.id && potionID != Potion.regeneration.id
                        && potionID != Potion.heal.id && potionID != Potion.resistance.id && potionID != Potion.fireResistance.id
                        && potionID != Potion.waterBreathing.id && potionID != Potion.invisibility.id && potionID != Potion.nightVision.id
                        && potionID != Potion.jump.id && potionID != Potion.damageBoost.id) {
                    effectsToRemove.add(effect);
                }
            }

            for (PotionEffect effect : effectsToRemove) {
                player.removePotionEffect(effect.getPotionID());
            }

            // Уменьшаем прочность
            itemStack.damageItem(50, player);
        }
        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                main.proxy.spawnParticle(EnumParticleType.SACRED, world,
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }
        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:temple", 1.0F, 1.0F);

        return itemStack;
    }
    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}
