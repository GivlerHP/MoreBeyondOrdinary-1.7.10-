package ru.givler.mbo.decor;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.client.render.decormodels.DecorAtlasSprite;
import sun.misc.Unsafe;

/** Actual model PNGs: identical atlas/particle pixels without moving legacy resources. */
public final class DecorAtlasSmoke {
 public static void main(String[] args)throws Exception {
  Pbuffer context=new Pbuffer(32,32,new PixelFormat(8,24,0),null,null);context.makeCurrent();
  StaticDecorPixelsSmoke.fakeMinecraft();
  Field access=Unsafe.class.getDeclaredField("theUnsafe");access.setAccessible(true);Unsafe unsafe=(Unsafe)access.get(null);
  Minecraft.getMinecraft().gameSettings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);
  TextureMap atlas=(TextureMap)unsafe.allocateInstance(TextureMap.class);
  Field entries=TextureMap.class.getDeclaredField("mapRegisteredSprites");entries.setAccessible(true);entries.set(atlas,new HashMap<>());
  int count=0,rectangles=0;
  Path assets=Paths.get("src/main/resources/assets/mbo");
  for(String folder:new String[]{"textures/models/decor","textures/blocks/models/decor"})try(java.util.stream.Stream<Path> paths=Files.walk(assets.resolve(folder))) {
   for(Path path:(Iterable<Path>)paths.filter(p->p.toString().endsWith(".png"))::iterator) {
    ResourceLocation source=new ResourceLocation("mbo",assets.relativize(path).toString().replace('\\','/'));
    DecorAtlasSprite icon=(DecorAtlasSprite)DecorAtlasSprite.register(atlas,source);
    icon.load(Minecraft.getMinecraft().getResourceManager(),source);
    BufferedImage image=ImageIO.read(path.toFile());
    if(icon.getIconWidth()!=image.getWidth()||icon.getIconHeight()!=image.getHeight()
       ||!Arrays.equals(icon.getFrameTextureData(0)[0],image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth())))
     throw new AssertionError("Atlas changed texture pixels: "+source);
    String name=folder.equals("textures/models/decor")?assets.resolve(folder).relativize(path).toString()
      :"instruments_of_torture/"+assets.resolve(folder).relativize(path);
    name=name.replace('\\','/').replace(".png","");
    BlockModels block=(BlockModels)unsafe.allocateInstance(BlockModels.class);ChunkDecorSmoke.set(block,"particleTextureName",name);
    block.registerBlockIcons(atlas);
    DecorAtlasSprite particle=(DecorAtlasSprite)block.getIcon(2,0);
    if(!particle.source.equals(source)||particle==icon)throw new AssertionError("Particle texture must be separate from model geometry: "+name);
    particle.load(Minecraft.getMinecraft().getResourceManager(),source);
    if(particle.getIconWidth()!=32||particle.getIconHeight()!=32)throw new AssertionError("Particle tile size");
    for(int pixel:particle.getFrameTextureData(0)[0])if((pixel>>>24)!=255)throw new AssertionError("Invisible particle texel: "+name);
    for(int level=0;level<=4;level++) {
     icon.generateMipmaps(level);particle.generateMipmaps(level);
     if(icon.getFrameTextureData(0).length!=level+1||particle.getFrameTextureData(0).length!=level+1)
      throw new AssertionError("Missing mipmap levels: "+source);
    }
    if(DecorAtlasSprite.register(atlas,source)!=icon)throw new AssertionError("Duplicate sprite registration");
    count++;if(image.getWidth()!=image.getHeight())rectangles++;
   }
  }
  context.destroy();System.out.println("PASS actual atlas and breaking-particle PNGs: "+count+" textures, "+rectangles+" rectangular, unchanged RGBA");
 }
}
