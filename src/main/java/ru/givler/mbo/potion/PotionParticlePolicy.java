package ru.givler.mbo.potion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

/** Calculates vanilla entity particle data from effects that allow particles. */
public final class PotionParticlePolicy {
    private PotionParticlePolicy() {}

    public static int color(Collection<PotionEffect> effects) {
        Collection<PotionEffect> visible = visibleEffects(effects);
        return visible.isEmpty() ? 0 : PotionHelper.calcPotionLiquidColor(visible);
    }

    public static boolean ambient(Collection<PotionEffect> effects) {
        Collection<PotionEffect> visible = visibleEffects(effects);
        return !visible.isEmpty() && PotionHelper.func_82817_b(visible);
    }

    public static void spawnParticle(World world, String name, double x, double y, double z,
            double motionX, double motionY, double motionZ, EntityLivingBase entity) {
        if (!MoreBeyondOrdinary.proxy.hideOwnPotionParticles(entity)) {
            world.spawnParticle(name, x, y, z, motionX, motionY, motionZ);
        }
    }

    private static Collection<PotionEffect> visibleEffects(Collection<PotionEffect> effects) {
        List<PotionEffect> visible = new ArrayList<PotionEffect>(effects.size());
        for (PotionEffect effect : effects) {
            int id = effect.getPotionID();
            Potion potion = id >= 0 && id < Potion.potionTypes.length
                    ? Potion.potionTypes[id] : null;
            if (!(potion instanceof PotionBasic)
                    || ((PotionBasic) potion).showsEntityParticles()) {
                visible.add(effect);
            }
        }
        return visible;
    }
}
