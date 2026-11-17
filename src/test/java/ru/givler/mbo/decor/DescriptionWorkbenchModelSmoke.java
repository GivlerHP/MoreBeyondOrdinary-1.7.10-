package ru.givler.mbo.decor;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoModel;

/** Actual GeckoLib geometry/UV loader and offscreen OpenGL rendering of the imported model. */
public final class DescriptionWorkbenchModelSmoke {
    public static void main(String[] args) throws Exception {
        int size = 512;
        Pbuffer buffer = new Pbuffer(size, size, new PixelFormat(8, 24, 0), null, null);
        try {
            buffer.makeCurrent(); StaticDecorPixelsSmoke.fakeMinecraft();
            ResourceLocation location = new ResourceLocation("mbo", "geo/description_workbench.geo.json");
            GeoModel model = GeoModelLoader.getInstance().loadModel(Minecraft.getMinecraft().getResourceManager(), location);
            BufferedImage texture = ImageIO.read(Paths.get("src/main/resources/assets/mbo/textures/models/decor/description_workbench.png").toFile());
            ByteBuffer data = BufferUtils.createByteBuffer(texture.getWidth() * texture.getHeight() * 4);
            for (int y = 0; y < texture.getHeight(); y++) for (int x = 0; x < texture.getWidth(); x++) {
                int c = texture.getRGB(x, y); data.put((byte)(c >> 16)).put((byte)(c >> 8)).put((byte)c).put((byte)(c >> 24));
            }
            data.flip(); GL11.glBindTexture(GL11.GL_TEXTURE_2D, GL11.glGenTextures());
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE); GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
            GL11.glViewport(0, 0, size, size); GL11.glClearColor(.15F, .17F, .2F, 1);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity(); GL11.glOrtho(-2, 2, -1, 3, -10, 10);
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity();
            GL11.glRotatef(25, 1, 0, 0); GL11.glRotatef(-35, 0, 1, 0); GL11.glTranslatef(0, 0, -.5F);
            StaticDecorPixelsSmoke.RENDERER.render(model, StaticDecorPixelsSmoke.TILE, 0, 1, 1, 1, 1);
            ByteBuffer pixels = BufferUtils.createByteBuffer(size * size * 4);
            GL11.glReadPixels(0, 0, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                int r = pixels.get() & 255, g = pixels.get() & 255, b = pixels.get() & 255, a = pixels.get() & 255;
                output.setRGB(x, size - y - 1, a << 24 | r << 16 | g << 8 | b);
            }
            ImageIO.write(output, "png", Paths.get("build/description-workbench-preview.png").toFile());
            if (GL11.glGetError() != GL11.GL_NO_ERROR) throw new AssertionError("Gecko model GL error");
            System.out.println("Converted workbench loaded and rendered with GeckoLib; preview saved.");
        } finally { buffer.destroy(); }
    }
}
