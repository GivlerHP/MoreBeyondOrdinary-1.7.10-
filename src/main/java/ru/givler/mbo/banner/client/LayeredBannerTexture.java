package ru.givler.mbo.banner.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.ResourceLocation;

public class LayeredBannerTexture extends AbstractTexture {
    private final ResourceLocation base;
    private final List<ResourceLocation> layers;
    private final List<Integer> colors;
    public LayeredBannerTexture(ResourceLocation base, List<ResourceLocation> layers, List<Integer> colors) {
        this.base=base; this.layers=layers; this.colors=colors;
    }
    @Override public void loadTexture(IResourceManager manager) throws IOException {
        deleteGlTexture();
        BufferedImage baseImage=read(manager,base);
        BufferedImage result=new BufferedImage(baseImage.getWidth(),baseImage.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics=result.getGraphics(); graphics.drawImage(baseImage,0,0,null);
        for(int i=0;i<layers.size() && i<colors.size();i++) {
            BufferedImage mask=read(manager,layers.get(i));
            float[] rgb=EntitySheep.fleeceColorTable[colors.get(i)&15];
            for(int y=0;y<mask.getHeight();y++) for(int x=0;x<mask.getWidth();x++) {
                int pixel=mask.getRGB(x,y), alpha=pixel>>>24;
                if(alpha!=0) {
                    int basePixel=baseImage.getRGB(x,y);
                    int r=(int)(((basePixel>>16)&255)*rgb[0]);
                    int g=(int)(((basePixel>>8)&255)*rgb[1]);
                    int b=(int)((basePixel&255)*rgb[2]);
                    mask.setRGB(x,y,(alpha<<24)|(r<<16)|(g<<8)|b);
                }
            }
            graphics.drawImage(mask,0,0,null);
        }
        graphics.dispose();
        TextureUtil.uploadTextureImage(getGlTextureId(),result);
    }
    private BufferedImage read(IResourceManager manager, ResourceLocation location) throws IOException {
        InputStream in=manager.getResource(location).getInputStream();
        try { return ImageIO.read(in); } finally { in.close(); }
    }
}
