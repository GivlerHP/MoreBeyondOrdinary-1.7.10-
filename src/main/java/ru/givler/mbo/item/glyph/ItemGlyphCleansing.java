package ru.givler.mbo.item.glyph;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.registry.PotionRegistry;

public class ItemGlyphCleansing extends ItemGlyphBasic {

    public ItemGlyphCleansing(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(120);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    private static int[] REMOVABLE_EFFECTS = null;

    private static int[] getRemovableEffects() {
        if (REMOVABLE_EFFECTS == null) {
            REMOVABLE_EFFECTS = new int[] {
                    Potion.invisibility.getId(),
                    Potion.poison.getId(),
                    Potion.wither.getId(),
                    Potion.confusion.getId(),
                    Potion.blindness.getId(),
                    PotionRegistry.Disarm.getId(),
                    PotionRegistry.DodgeHit.getId()
            };
        }
        return REMOVABLE_EFFECTS;
    }

    private boolean isForceRemove(int potionID) {
        for (int id : getRemovableEffects()) {
            if (id == potionID) return true;
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            List<PotionEffect> effectsToRemove = new ArrayList<>();

            for (Object obj : player.getActivePotionEffects()) {
                PotionEffect effect = (PotionEffect) obj;
                int potionID = effect.getPotionID();
                Potion potion = Potion.potionTypes[potionID];
                if (isForceRemove(potionID)) {
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
                MoreBeyondOrdinary.proxy.spawnParticle(
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
}
