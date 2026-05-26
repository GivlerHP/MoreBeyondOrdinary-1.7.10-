package ru.givler.mbo.item.glyph;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
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
                if (isForceRemove(effect.getPotionID())) {
                    effectsToRemove.add(effect);
                }
            }

            for (PotionEffect effect : effectsToRemove) {
                player.removePotionEffect(effect.getPotionID());
            }

            itemStack.damageItem(50, player);
            world.playSoundAtEntity(player, "mbo:temple", 1.0F, 1.0F);
            player.swingItem();

            PacketSpawnParticle.send(EnumParticleType.SACRED, world, player, 30, 2.0, 2.0, 2.0,1.0,   0.0, 0.0, 0.0);
        }

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
    }
}
