package ru.givler.mbo.client.render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
/** Shared enchanted glint for flat shelf items and items carried by animals. */
public final class ItemSpriteGlint {
    private static final net.minecraft.util.ResourceLocation TEXTURE =
            new net.minecraft.util.ResourceLocation("textures/misc/enchanted_item_glint.png");
    private ItemSpriteGlint() {}
    public static void render() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glColor4f(.38F, .19F, .608F, 1);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        try {
            for (int pass = 0; pass < 2; pass++) {
                GL11.glPushMatrix();
                try {
                    GL11.glScalef(.125F, .125F, .125F);
                    long period = pass == 0 ? 3000 : 4873;
                    float offset = Minecraft.getSystemTime() % period / (float) period * 8;
                    GL11.glTranslatef(pass == 0 ? offset : -offset, 0, 0);
                    GL11.glRotatef(pass == 0 ? -50 : 10, 0, 0, 1);
                    ItemRenderer.renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 256, 256, .0625F);
                } finally { GL11.glPopMatrix(); }
            }
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopAttrib();
        }
        }
}
