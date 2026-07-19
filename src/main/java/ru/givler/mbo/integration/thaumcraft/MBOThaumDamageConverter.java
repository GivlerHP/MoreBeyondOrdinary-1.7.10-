package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.thaumcraft.TCDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

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
    public void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.source;
        if (source == null || source instanceof TCDamageSource) return;

        Entity projectile = source.getSourceOfDamage();
        if (projectile == null) return;

        String name = projectile.getClass().getName().toLowerCase();
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
        replaceSource(event, converted);
    }

    private static void replaceSource(LivingHurtEvent event, TCDamageSource source) {
        if (eventSourceField == null) return;
        try {
            eventSourceField.set(event, source);
        } catch (Exception ignored) {
        }
    }
}
