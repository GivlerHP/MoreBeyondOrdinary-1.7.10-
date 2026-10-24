package ru.givler.mbo.client.texture;

import com.google.gson.*;
import java.awt.image.BufferedImage;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.core.TextureInterpolationTransformer;

public final class TextureInterpolationSmoke {
  static final int[] COLORS={0xff000000,0xffff0000,0xff00ff00,0xff0000ff};
  static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
  static final class Sprite extends TextureAtlasSprite {
    Sprite(){super("mbo:test_interpolate");}
    void time(int position,int ticks){frameCounter=position;tickCounter=ticks;}
  }
  static Sprite sprite(String json) {
    JsonElement element=new JsonParser().parse(json);
    AnimationMetadataSection metadata=new AnimationMetadataSectionSerializer().deserialize(element,null,null);
    TextureInterpolation.read(metadata,element);
    Sprite sprite=new Sprite();BufferedImage strip=new BufferedImage(2,8,BufferedImage.TYPE_INT_ARGB);
    for(int frame=0;frame<4;frame++)for(int y=0;y<2;y++)for(int x=0;x<2;x++)strip.setRGB(x,frame*2+y,COLORS[frame]);
    sprite.loadSprite(new BufferedImage[]{strip},metadata,false);
    TextureInterpolation.loaded(sprite,metadata);
    return sprite;
  }
  public static void main(String[] args)throws Exception {
    Sprite prismarine=sprite("{\"frametime\":300,\"interpolate\":true,\"frames\":[0,1,0,2,0,3,0,1,2,1,3,1,0,2,1,2,3,2,0,3,1,3]}");
    prismarine.time(5,150);int[][] first=TextureInterpolation.frame(prismarine);
    check(first[0][0]==0xff00007f,"provided prismarine sequence and 300-tick timing");
    prismarine.time(21,150);check(TextureInterpolation.frame(prismarine)[0][0]==0xff00007f,"last frame wraps to first");
    check(first==TextureInterpolation.frame(prismarine),"reuse frame buffers");
    check(prismarine.getFrameTextureData(3)[0][0]==COLORS[3],"source pixels remain immutable");
    Sprite automatic=sprite("{\"frametime\":10,\"interpolate\":true}");
    automatic.time(0,5);check(TextureInterpolation.frame(automatic)[0][0]==0xff7f0000,"automatic frame list survives vanilla metadata replacement");
    Sprite timed=sprite("{\"frametime\":300,\"interpolate\":true,\"frames\":[{\"index\":0,\"time\":4},{\"index\":1,\"time\":8}]}");
    timed.time(0,2);check(TextureInterpolation.frame(timed)[0][0]==0xff7f0000,"individual frame duration");
    timed.time(1,2);check(TextureInterpolation.frame(timed)[0][0]==0xffbf0000,"next individual frame duration and wrap");
    // Install representative mip levels without loading TextureUtil's GL-dependent initializer.
    timed.setFramesTextureData(Arrays.asList(new int[][]{new int[]{COLORS[0]},new int[]{COLORS[0]}},new int[][]{new int[]{COLORS[1]},new int[]{COLORS[1]}}));
    timed.time(0,2);check(TextureInterpolation.frame(timed)[1][0]==0xff7f0000,"mipmap interpolation");
    Sprite repeat=sprite("{\"frametime\":10,\"interpolate\":true,\"frames\":[0,0,1]}");
    repeat.time(0,5);check(TextureInterpolation.frame(repeat)==null,"identical consecutive frames avoid uploads");
    for(String flag:new String[]{",\"interpolate\":false",""}) {
      Sprite off=sprite("{\"frametime\":10"+flag+"}");off.time(0,5);
      check(TextureInterpolation.frame(off)==null,"false or absent flag preserves vanilla");
    }
    automatic.time(0,0);check(TextureInterpolation.frame(automatic)==null,"keyframe tick stays owned by vanilla");
    TextureInterpolation.clear(prismarine);check(TextureInterpolation.frame(prismarine)==null,"reload clears interpolation buffers");
    check(TextureInterpolation.blend(0x40000000,0xffff0000,.5)==0x407f0000,"vanilla alpha semantics");
    JsonElement json=new JsonParser().parse("{\"interpolate\":true}");
    AnimationMetadataSection meta=TextureInterpolation.read(new AnimationMetadataSection(Collections.emptyList(),2,2,10),json);
    check(TextureInterpolation.write(new JsonObject(),meta).getAsJsonObject().get("interpolate").getAsBoolean(),"metadata round trip");
    boolean invalid=false;try {TextureInterpolation.read(meta,new JsonParser().parse("{\"interpolate\":\"yes\"}"));}catch(JsonParseException expected){invalid=true;}
    check(invalid,"invalid interpolate value rejected");
    try {
      TextureAtlasSprite nativeSprite=(TextureAtlasSprite)Class.forName("jss.notfine.render.InterpolatedIcon").getConstructor(String.class).newInstance("mbo:native_interpolate");
      TextureInterpolation.loaded(nativeSprite,meta);
      check(TextureInterpolation.frame(nativeSprite)==null,"Angelica native interpolator is not updated twice");
      System.out.println("Angelica native InterpolatedIcon ownership passed");
    }catch(ClassNotFoundException absent) {System.out.println("Running standalone interpolation without Angelica");}
    for(String target:new String[]{"net.minecraft.client.resources.data.AnimationMetadataSectionSerializer","net.minecraft.client.renderer.texture.TextureAtlasSprite"}) {
      java.io.ByteArrayOutputStream data=new java.io.ByteArrayOutputStream();
      try(java.io.InputStream in=TextureInterpolationSmoke.class.getClassLoader().getResourceAsStream(target.replace('.','/')+".class")) {
        byte[] block=new byte[8192];for(int n;(n=in.read(block))>=0;)data.write(block,0,n);
      }
      TextureInterpolationTransformer transformer=new TextureInterpolationTransformer();
      byte[] transformed=transformer.transform(target,target,data.toByteArray());
      check(!Arrays.equals(transformed,data.toByteArray()),"actual vanilla target patched");
      check(Arrays.equals(transformed,transformer.transform(target,target,transformed)),"transformer is idempotent");
      ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);
      for(MethodNode method:node.methods)if((method.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer(new BasicVerifier()).analyze(node.name,method);
    }
    System.out.println("Texture interpolation: supplied prismarine timeline, auto/custom frames, mipmaps, immutable pixels, buffer reuse, alpha, disabled/reload and actual ASM passed");
  }
}
