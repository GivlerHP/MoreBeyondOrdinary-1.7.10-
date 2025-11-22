package ru.givler.mbo.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import net.minecraftforge.client.IItemRenderer;
import ru.givler.mbo.item.ItemBow;

public class RenderCrossbow implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);

        // Проверяем, если лук используется
        if (mc.thePlayer.getItemInUse() == item) {
            // Получаем данные о натяжении
            int drawTime = item.getMaxItemUseDuration() - mc.thePlayer.getItemInUseCount();
            int frame = 0;

            // Определяем, какой кадр показывать в зависимости от времени натяжения
            if (drawTime >= 25) {
                frame = 3;  // Лук сильно натянут
            } else if (drawTime >= 14) {
                frame = 2;  // Лук средне натянут
            } else if (drawTime > 0) {
                frame = 1;  // Лук немного натянут
            }

            GL11.glPushMatrix();

            // Позиционирование и масштабирование для первого лица
            if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {

                GL11.glTranslatef(0.0F, -0.1F, 0.0F);  // Позиционирование для первого лица
            } else {
                // Позиционирование и масштабирование для второго лица
               GL11.glRotatef(90, 0.0F, -0.1F, 0.0F);
               GL11.glRotatef(320, 0.0F, 0.0F, 0.1F);

                GL11.glTranslatef(-1.2F, -1.2F, -0.65F);  // Позиционирование для лука, если он не используется
                GL11.glScalef(1.5F, 1.5F, 1.5F);  // Масштабирование для нормального отображения
            }

            // Рендерим иконку лука
            IIcon icon = ((ItemBow) item.getItem()).icons[frame];
            Tessellator tessellator = Tessellator.instance;
            ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.05F);
            GL11.glPopMatrix();
        } else {
            // Если лук не используется
            GL11.glPushMatrix();

            // Позиционирование и масштабирование для лука в покое
            if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                GL11.glTranslatef(0.0F, -0.1F, 0.0F);  // Позиционирование для первого лица
            } else {
                GL11.glRotatef(270, 0.0F, 1.3F, 0.5F);
                GL11.glTranslatef(-1.0F, -0.7F, -0.5F);  // Позиционирование для лука, если он не используется
                GL11.glScalef(1.5F, 1.5F, 1.5F);  // Масштабирование для нормального отображения
            }

            IIcon icon = ((ItemBow) item.getItem()).icons[0];  // Иконка без натяжения
            Tessellator tessellator = Tessellator.instance;
            ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.05F);
            GL11.glPopMatrix();
        }
    }
}
