package ru.givler.mbo.potion;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.PotionRegistry;

import java.util.Random;

public class Hex extends Potion {

    private static final ResourceLocation potionIcon = new ResourceLocation(main.MODID, "textures/gui/hex_icon.png");
    public static int potionId;

    public Hex(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.hex");
        potionId = id;
    }

    @Override
    public void performEffect(EntityLivingBase entitylivingbase, int strength) {
        // Логика здесь не требуется, т.к. эффекты применяются через событие урона.
    }

    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.renderEngine.bindTexture(potionIcon); // Привязываем текстуру
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    // Метод для рисования иконки в инвентаре
    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        float f = 1F / (float) textureWidth;
        float f1 = 1F / (float) textureHeight;
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double) (x), (double) (y + height), 0, (double) ((float) (u) * f), (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV((double) (x + width), (double) (y + height), 0, (double) ((float) (u + width) * f), (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV((double) (x + width), (double) (y), 0, (double) ((float) (u + width) * f), (double) ((float) (v) * f1));
        tessellator.addVertexWithUV((double) (x), (double) (y), 0, (double) ((float) (u) * f), (double) ((float) (v) * f1));
        tessellator.draw();
    }

    // Класс-обработчик событий
    public static class Handler {

        private static final Random rand = new Random();

        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onEntityAttacked(LivingHurtEvent event) {
            if (event.source.getEntity() instanceof EntityPlayer) { // Проверка на то, что атакует игрок
                EntityLivingBase target = event.entityLiving; // Цель атаки

                if (target instanceof EntityLivingBase) {
                    EntityPlayer player = (EntityPlayer) event.source.getEntity();
                    if (player.isPotionActive(Potion.potionTypes[potionId])) {
                        PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[potionId]);
                        int level = effect.getAmplifier() + 1;

                        double chance = 0.1 * level; // Например, 10% шанс за каждый уровень эффекта
                        if (rand.nextDouble() <= chance) {
                            int randomEffect = rand.nextInt(6); // Выбираем случайный эффект

                            switch (randomEffect) {
                                case 0:
                                    target.addPotionEffect(new PotionEffect(Potion.weakness.id, 100, 0)); // Слабость
                                    break;
                                case 1:
                                    target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 0)); // Замедление
                                    break;
                                case 2:
                                    target.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0)); // Слепота
                                    break;
                                case 3:
                                    target.addPotionEffect(new PotionEffect(Potion.hunger.id, 100, 0)); // Голод
                                    break;
                                case 4:
                                    target.addPotionEffect(new PotionEffect(Potion.poison.id, 100, 0)); //Яд
                                    break;
                                case 5:
                                    target.addPotionEffect(new PotionEffect(PotionRegistry.DodgeHit.id, 100, 0)); //Отмена уклонения
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }
}