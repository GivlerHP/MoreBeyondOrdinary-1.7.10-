package ru.givler.mbo.lootcontainer.action;

import net.minecraft.potion.Potion;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.List;

final class LootContainerActionRuntime {
    private LootContainerActionRuntime() {
    }

    static int parseCountExpression(String expr, World world) {
        if (expr == null || expr.trim().isEmpty()) return 1;
        String trimmed = expr.trim();
        int dash = trimmed.indexOf('-');
        if (dash > 0 && dash < trimmed.length() - 1) {
            try {
                int min = Integer.parseInt(trimmed.substring(0, dash).trim());
                int max = Integer.parseInt(trimmed.substring(dash + 1).trim());
                if (max < min) {
                    int tmp = min;
                    min = max;
                    max = tmp;
                }
                if (world == null) return Math.max(0, min);
                return min + world.rand.nextInt(Math.max(1, max - min + 1));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        try {
            return Math.max(0, Integer.parseInt(trimmed));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    static Potion resolvePotion(String potionId) {
        if (potionId == null || potionId.trim().isEmpty()) return null;
        try {
            int id = Integer.parseInt(potionId.trim());
            if (id >= 0 && id < Potion.potionTypes.length) {
                return Potion.potionTypes[id];
            }
        } catch (NumberFormatException ignored) {
        }
        String lowered = potionId.trim().toLowerCase();
        for (Potion potion : Potion.potionTypes) {
            if (potion == null) continue;
            String name = potion.getName();
            if (name != null && name.toLowerCase().contains(lowered)) {
                return potion;
            }
        }
        return null;
    }

    static void playExplosionFeedback(World world, double x, double y, double z, float radius) {
        if (world == null) return;
        world.playSoundEffect(
                x, y, z,
                "random.explode",
                1.0F,
                (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F
        );
        if (!(world instanceof WorldServer)) return;

        WorldServer serverWorld = (WorldServer) world;
        serverWorld.func_147487_a("hugeexplosion", x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        int burst = Math.max(4, (int) Math.ceil(Math.max(0.1F, radius) * 2.0F));
        for (int i = 0; i < burst; i++) {
            double ox = (world.rand.nextDouble() - 0.5D) * radius;
            double oy = (world.rand.nextDouble() - 0.5D) * Math.max(0.5D, radius * 0.5D);
            double oz = (world.rand.nextDouble() - 0.5D) * radius;
            serverWorld.func_147487_a("largeexplode", x + ox, y + oy, z + oz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    static void applyExplosionKnockback(World world, double x, double y, double z, float radius) {
        if (world == null) return;
        float normalizedRadius = Math.max(0.1F, radius);
        int radiusCeil = (int) Math.ceil(normalizedRadius);
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
                x - radiusCeil, y - radiusCeil, z - radiusCeil,
                x + radiusCeil, y + radiusCeil, z + radiusCeil
        );
        List list = world.getEntitiesWithinAABB(Entity.class, area);
        for (Object obj : list) {
            if (!(obj instanceof Entity)) continue;
            Entity entity = (Entity) obj;
            if (entity.isDead) continue;
            double dx = entity.posX - x;
            double dz = entity.posZ - z;
            double distSq = dx * dx + dz * dz;
            if (distSq < 1.0E-6D) continue;
            double distance = Math.sqrt(distSq);
            if (distance > normalizedRadius) continue;

            double intensity = 1.0D - (distance / normalizedRadius);
            double horizontalStrength = 0.45D * intensity;
            double verticalStrength = 0.15D + 0.12D * intensity;

            entity.addVelocity((dx / distance) * horizontalStrength, verticalStrength, (dz / distance) * horizontalStrength);
            entity.velocityChanged = true;
        }
    }
}
