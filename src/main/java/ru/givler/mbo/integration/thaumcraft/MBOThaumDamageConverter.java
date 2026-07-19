package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.thaumcraft.TCDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import thaumcraft.common.lib.utils.ProtectionUtils;

import java.lang.reflect.Field;

/** Converts MBO and its wizardry staff projectiles to the shared MF damage contract. */
public class MBOThaumDamageConverter {
    private static Field eventSourceField;

    static {
        try {
            eventSourceField = LivingHurtEvent.class.getField("source");
            eventSourceField.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.source;
        Entity projectile = source == null ? null : source.getSourceOfDamage();
        if (projectile == null || !isMBOProjectile(projectile)) return;

        Entity attacker = source.getEntity();
        if (!ProtectionUtils.canEntityDamage(attacker, event.entityLiving)) {
            event.setCanceled();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosionStart(ExplosionEvent.Start event) {
        if (event.explosion == null || event.explosion.exploder == null) return;
        if (isMBOProjectile(event.explosion.exploder)
                && !ProtectionUtils.canEntityExplode(event.explosion.exploder)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.source;
        if (source == null || source instanceof TCDamageSource) return;

        Entity projectile = source.getSourceOfDamage();
        if (projectile == null) return;

        String name = projectile.getClass().getName().toLowerCase();
        if (!isMBOProjectile(projectile)) return;
        String focus;
        String aspect;
        boolean fire = false;

        if (name.contains("entityexplosiveorb")) {
            focus = "fire";
            aspect = "Ignis";
            fire = true;
        } else if (name.contains("thunderbolt") || name.contains("lightning")) {
            focus = "lightning";
            aspect = "Aer";
        } else if (name.contains("iceshard") || name.contains("icecharge") || name.contains("frost")) {
            focus = "frost";
            aspect = "Gelum";
        } else if (name.contains("entitydarkmatter") || name.contains("entitydiffusion")
                || name.contains("entitydarkmoonorb")) {
            focus = "darkness";
            aspect = "Perditio";
        } else if (name.contains("entitypech") || name.contains("entityhomingshard")
                || name.contains("entitylightmatter") || name.contains("forceorb")) {
            focus = "magic";
            aspect = "Praecantatio";
        } else {
            return;
        }

        Entity caster = source.getEntity();
        TCDamageSource converted = TCDamageSource.create(focus + "_damage", projectile, caster)
                .setFocusId(focus)
                .setPrimaryAspect(aspect)
                .setMagicDamage(!fire)
                .setIsFireDamage(fire)
                .setArmorPenetration(0.0F);
        notifyTreatyPvp(caster, event.entityLiving, event.ammount);
        replaceSource(event, converted);
    }

    /**
     * Bukkit has no damager event for several direct Thaumcraft attacks. Keep
     * the bridge optional so this mod also works when TreatyPvP is absent.
     */
    private static void notifyTreatyPvp(Entity caster, EntityLivingBase target, float damage) {
        if (caster == null || target == null) return;
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object manager = bukkit.getMethod("getPluginManager").invoke(null);
            Object plugin = manager.getClass().getMethod("getPlugin", String.class)
                    .invoke(manager, "TreatyPvP");
            if (plugin == null) return;
            Object bukkitCaster = caster.getClass().getMethod("getBukkitEntity").invoke(caster);
            Object bukkitTarget = target.getClass().getMethod("getBukkitEntity").invoke(target);
            Class<?> bukkitEntity = Class.forName("org.bukkit.entity.Entity");
            plugin.getClass().getMethod("notifyMagicDamage", bukkitEntity, bukkitEntity, double.class)
                    .invoke(plugin, bukkitCaster, bukkitTarget, (double) damage);
        } catch (Throwable ignored) {
            // Optional server-side integration; damage handling must continue.
        }
    }

    private static boolean isMBOProjectile(Entity projectile) {
        String name = projectile.getClass().getName().toLowerCase();
        return name.startsWith("ru.givler.mbo.integration.thaumcraft.entities")
                || name.contains("entitythunderbolt")
                || name.contains("entityiceshard")
                || name.contains("entityicecharge")
                || name.contains("entityforceorb")
                || name.contains("entityexplosiveorb");
    }

    private static void replaceSource(LivingHurtEvent event, TCDamageSource source) {
        if (eventSourceField == null) return;
        try {
            eventSourceField.set(event, source);
        } catch (Exception ignored) {
        }
    }
}
