package ru.givler.mbo.potion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.main;

public class Phoenix extends Potion {
    private static final ResourceLocation icon =
            new ResourceLocation(main.MODID, "textures/gui/phoenix_icon.png");
    public static int potionId;

    public Phoenix(int id, boolean isBadEffect, int liquidColor) {
        super(id, isBadEffect, liquidColor);
        this.setPotionName("potion.phoenix");
        potionId = id;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.renderEngine.bindTexture(icon);
        drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    @SideOnly(Side.CLIENT)
    private static void drawTexturedRect(int x, int y, int u, int v,
                                         int width, int height,
                                         int texW, int texH) {
        float f  = 1F / texW;
        float f1 = 1F / texH;
        net.minecraft.client.renderer.Tessellator t = net.minecraft.client.renderer.Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x,         y + height, 0, u * f,             (v + height) * f1);
        t.addVertexWithUV(x + width, y + height, 0, (u + width) * f,   (v + height) * f1);
        t.addVertexWithUV(x + width, y,          0, (u + width) * f,   v * f1);
        t.addVertexWithUV(x,         y,          0, u * f,             v * f1);
        t.draw();
    }
}
