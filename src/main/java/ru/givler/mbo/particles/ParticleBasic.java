package ru.givler.mbo.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import ru.givler.mbo.main;

public class ParticleBasic extends EntityFX {

    private static final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

    // Конструктор для частицы с текстурой
    public ParticleBasic(World world, double x, double y, double z, String texturePath, int maxAge, float size, float motionX, float motionY, float motionZ) {
        super(world, x, y, z);

        // Загружаем текстуру для частицы с помощью ResourceLocation
        ResourceLocation texture = new ResourceLocation(main.MODID + ":" + texturePath);

        // Устанавливаем индекс текстуры для частицы
        this.setParticleTextureIndex(0); // В 1.7.10 текстура будет привязана через индекс

        // Устанавливаем параметры размера и движения
        this.setSize(size, size);
        this.particleMaxAge = maxAge;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;

        // Применяем текстуру к частице
        textureManager.bindTexture(texture);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    // Метод для создания и отображения частицы
    public static void registerParticle(String name, String texture, int maxAge, float size, float motionX, float motionY, float motionZ) {
        // Получаем координаты игрока
        World world = Minecraft.getMinecraft().theWorld;
        double x = Minecraft.getMinecraft().thePlayer.posX;  // Получаем координаты игрока
        double y = Minecraft.getMinecraft().thePlayer.posY + 1.0; // Немного выше головы игрока
        double z = Minecraft.getMinecraft().thePlayer.posZ;

        // Спавним частицу
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBasic(world, x, y, z, texture, maxAge, size, motionX, motionY, motionZ));
    }
}
