package ru.givler.mbo.potion;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import ru.givler.mbo.main;

public class Dodge extends Potion {
    private static final ResourceLocation potionIcon = new ResourceLocation(main.MODID, "textures/gui/dodge_icon.png");
    public static int potionId;

    public Dodge(int id, boolean isBadEffect, int liquidColour) {
        super(id, isBadEffect, liquidColour);
        this.setPotionName("potion.dodge");
        potionId = id;

    }

    // Чтобы эффект считался «всегда готовым»
    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        // Рисуем иконку эффекта в инвентаре
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    @SideOnly(Side.CLIENT)
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

    // Обработчик события урона — находится в том же классе
    public static class DodgeServerHandler {
        @SubscribeEvent
        public void onLivingAttack(LivingAttackEvent event) {
            if (!(event.entityLiving instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) event.entityLiving;

            if (!player.isPotionActive(potionId)) return;

            DamageSource source = event.source;
            if (
                    source == DamageSource.fall ||
                            source == DamageSource.inFire ||
                            source == DamageSource.onFire ||
                            source == DamageSource.lava ||
                            source == DamageSource.magic ||
                            source == DamageSource.drown ||
                            source == DamageSource.starve
            ) {
                return;
            }

            PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[potionId]);
            int level = effect.getAmplifier() + 1;
            double chance = 0.1 * level;

            if (player.worldObj.rand.nextDouble() < chance) {
                event.setCanceled(true);
                player.worldObj.playSoundAtEntity(player, "mbo:coldring", 1.0F, 1.0F);
            }
        }
    }
}
