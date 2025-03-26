package ru.givler.mbo.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import ru.givler.mbo.particles.ParticleBasic;

public class ParticleRegistry {

    public static void init() {
        // Регистрируем частицы с нужными параметрами (текстура, размер, максимальный возраст)
        ParticleBasic.registerParticle("fire_particle", "fire_texture", 100, 0.5F, 0.1F, 0.0F, 0.1F);
    }
}

