package ru.givler.mbo.integration.thaumcraft.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMoonOrb;

public class DarkMoonCastQueue {

    private static final List<PendingCast> pending = new ArrayList<PendingCast>();

    public static void queueCast(World world, EntityLivingBase caster, EntityLivingBase target,
                                 int sphereCount, int intervalTicks, int strength) {
        if (sphereCount <= 0) {
            return;
        }
        pending.add(new PendingCast(world, caster, target, sphereCount, intervalTicks, strength));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Iterator<PendingCast> it = pending.iterator();
        while (it.hasNext()) {
            PendingCast cast = it.next();

            if (cast.caster == null || cast.caster.isDead || cast.remaining <= 0) {
                it.remove();
                continue;
            }

            cast.tickCounter++;
            if (cast.tickCounter >= cast.intervalTicks) {
                cast.tickCounter = 0;

                EntityDarkMoonOrb orb = (cast.target != null && !cast.target.isDead)
                        ? new EntityDarkMoonOrb(cast.world, cast.caster, cast.target, cast.strength)
                        : new EntityDarkMoonOrb(cast.world, cast.caster, cast.strength);

                cast.world.spawnEntityInWorld(orb);

                // звук на каждый отдельный запуск, с небольшим случайным разбросом высоты тона
                float pitch = 0.9F + cast.world.rand.nextFloat() * 0.3F;
                cast.world.playSoundAtEntity(orb, "mbo:shard", 0.5F, pitch);

                cast.remaining--;
                if (cast.remaining <= 0) {
                    it.remove();
                }
            }
        }
    }

    private static class PendingCast {
        final World world;
        final EntityLivingBase caster;
        final EntityLivingBase target;
        int remaining;
        final int intervalTicks;
        final int strength;
        int tickCounter;

        PendingCast(World world, EntityLivingBase caster, EntityLivingBase target,
                    int remaining, int intervalTicks, int strength) {
            this.world = world;
            this.caster = caster;
            this.target = target;
            this.remaining = remaining;
            this.intervalTicks = intervalTicks;
            this.strength = strength;
            this.tickCounter = intervalTicks;
        }
    }
}