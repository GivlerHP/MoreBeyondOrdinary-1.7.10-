package ru.givler.mbo.client.render.decormodels;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/** Atlas view of the actual Gecko PNG, including rectangular model textures. */
public final class DecorAtlasSprite extends TextureAtlasSprite {
    public final ResourceLocation source;
    private final boolean particles;
    private DecorAtlasSprite(String name,ResourceLocation source,boolean particles) {super(name);this.source=source;this.particles=particles;}
    public static IIcon register(IIconRegister registry,ResourceLocation source) {
        return register(registry,source,false);
    }
    public static IIcon registerParticles(IIconRegister registry,ResourceLocation source) {
        return register(registry,source,true);
    }
    private static IIcon register(IIconRegister registry,ResourceLocation source,boolean particles) {
        String name=source.getResourceDomain()+":decor_atlas/"+(particles?"particles/":"")+source.getResourcePath().replace(".png","");
        if(!(registry instanceof TextureMap))return registry.registerIcon(name);
        TextureMap map=(TextureMap)registry;
        TextureAtlasSprite existing=map.getTextureExtry(name);
        if(existing!=null)return existing;
        DecorAtlasSprite sprite=new DecorAtlasSprite(name,source,particles);map.setTextureEntry(name,sprite);return sprite;
    }
    @Override public boolean hasCustomLoader(IResourceManager manager,ResourceLocation location){return true;}
    @Override public boolean load(IResourceManager manager,ResourceLocation ignored) {
        try(InputStream stream=manager.getResource(source).getInputStream()) {
            BufferedImage image=ImageIO.read(stream);
            if(image==null)throw new IOException("Unreadable PNG: "+source);
            setIconWidth(image.getWidth());setIconHeight(image.getHeight());
            // Gecko UVs address the entire PNG; a rectangular image isn't an animation strip.
            int[] pixels=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
            if(particles) {
                int visible=0;
                for(int pixel:pixels)if((pixel>>>24)>=128)pixels[visible++]=pixel;
                int[] opaque=new int[32*32];
                for(int i=0;i<opaque.length;i++)opaque[i]=visible==0?0xff808080:0xff000000|pixels[(int)(i*(long)visible/opaque.length)]&0xffffff;
                pixels=opaque;setIconWidth(32);setIconHeight(32);
            }
            setFramesTextureData(new ArrayList<int[][]>(Collections.singletonList(new int[][]{pixels})));
            return false;
        }catch(IOException error){throw new IllegalStateException("Cannot load decor atlas texture "+source,error);}
    }
    @Override public void generateMipmaps(int levels) {
        // TextureUtil assumes slots 0..levels exist, even when only level 0
        // was loaded. Missing levels must be null slots, not a shorter array.
        for(int frame=0;frame<framesTextureData.size();frame++) {
            int[][] data=(int[][])framesTextureData.get(frame);
            if(data.length<levels+1)framesTextureData.set(frame,Arrays.copyOf(data,levels+1));
        }
        super.generateMipmaps(levels);
    }
}
