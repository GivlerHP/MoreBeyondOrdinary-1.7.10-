package ru.givler.mbo.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ru.givler.mbo.main;

public class Vampirism extends Potion {

    private static final ResourceLocation potionIcon = new ResourceLocation(main.MODID, "textures/gui/vampirism_icon.png");
    public static int potionId; // Статический ID для проверки наличия эффекта

    public Vampirism(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.vampirism");
        potionId = id; // Сохраняем ID для использования в обработчике событий
        MinecraftForge.EVENT_BUS.register(new VampirismEventHandler()); // Регистрируем обработчик событий
    }

    @Override
    public void performEffect(EntityLivingBase entitylivingbase, int strength) {
        // Основной эффект будет применяться через систему событий Forge
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        // Этот эффект не нуждается в периодическом вызове performEffect()
        return false;
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        // Рисуем иконку эффекта в инвентаре
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    // Рисуем иконку на экране
    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
        tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
        tessellator.draw();
    }

    // Внутренний класс для обработки событий урона
    public static class VampirismEventHandler {

        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent event) {

            if (event.source.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();

                // Проверяем наличие эффекта вампиризма у атакующего
                if (attacker.isPotionActive(potionId)) {
                    int level = attacker.getActivePotionEffect(Potion.potionTypes[potionId]).getAmplifier();

                    float vampirismPercent = 0.03F + (level * 0.015F);
                    float healAmount = event.ammount * vampirismPercent;

                    // Восстанавливаем здоровье атакующему
                    if (attacker instanceof EntityPlayer) {
                        ((EntityPlayer) attacker).heal(healAmount);
                    } else {
                        float currentHealth = attacker.getHealth();
                        attacker.setHealth(Math.min(currentHealth + healAmount, attacker.getMaxHealth()));
                    }

                }
            }
        }
    }
}