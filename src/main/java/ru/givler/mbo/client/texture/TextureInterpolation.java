package ru.givler.mbo.client.texture;

import com.google.gson.*;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.AnimationMetadataSection;

/** Standard mcmeta interpolation; native Angelica interpolated sprites keep their own updater. */
public final class TextureInterpolation {
  private static final Map<AnimationMetadataSection,Boolean> metadata=Collections.synchronizedMap(new WeakHashMap<AnimationMetadataSection,Boolean>());
  private static final Map<TextureAtlasSprite,State> sprites=Collections.synchronizedMap(new WeakHashMap<TextureAtlasSprite,State>());
  private static final class Fields {
    static final Field METADATA=ReflectionHelper.findField(TextureAtlasSprite.class,"animationMetadata","field_110982_k");
    static final Field FRAME=ReflectionHelper.findField(TextureAtlasSprite.class,"frameCounter","field_110973_g");
    static final Field TICK=ReflectionHelper.findField(TextureAtlasSprite.class,"tickCounter","field_110983_h");
  }
  private static final class State { int[][] pixels; }
  private TextureInterpolation() {}

  public static AnimationMetadataSection read(AnimationMetadataSection section,JsonElement json) {
    if(json!=null&&json.isJsonObject()&&json.getAsJsonObject().has("interpolate")) {
      JsonElement value=json.getAsJsonObject().get("interpolate");
      if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isBoolean())
        throw new JsonParseException("animation.interpolate must be a boolean");
      metadata.put(section,value.getAsBoolean());
    }
    return section;
  }
  public static void loaded(TextureAtlasSprite sprite,AnimationMetadataSection original) {
    sprites.remove(sprite);
    if(original==null||!Boolean.TRUE.equals(metadata.get(original))||nativeInterpolation(sprite))return;
    sprites.put(sprite,new State());
  }
  public static JsonElement write(JsonElement json,AnimationMetadataSection section) {
    Boolean value=metadata.get(section);
    if(value!=null&&json!=null&&json.isJsonObject())json.getAsJsonObject().addProperty("interpolate",value);
    return json;
  }
  private static boolean nativeInterpolation(TextureAtlasSprite sprite) {
    for(Class<?> type=sprite.getClass();type!=null;type=type.getSuperclass())
      if(type.getName().equals("jss.notfine.render.InterpolatedIcon"))return true;
    return false;
  }
  public static void clear(TextureAtlasSprite sprite) { sprites.remove(sprite); }

  /** Computes only; the normal animation tick owns counters and discrete frame transitions. */
  public static int[][] frame(TextureAtlasSprite sprite) {
    State state=sprites.get(sprite);if(state==null)return null;
    try {
      AnimationMetadataSection section=(AnimationMetadataSection)Fields.METADATA.get(sprite);
      if(section==null)return null;
      int position=Fields.FRAME.getInt(sprite),tick=Fields.TICK.getInt(sprite);
      int count=section.getFrameCount();if(count==0)count=sprite.getFrameCount();
      if(count<2||position<0||position>=count||tick<=0)return null;
      int duration=section.getFrameTimeSingle(position);if(duration<=1||tick>=duration)return null;
      int current=section.getFrameCount()==0?position:section.getFrameIndex(position);
      int next=section.getFrameCount()==0?(position+1)%count:section.getFrameIndex((position+1)%count);
      if(current==next||current<0||next<0||current>=sprite.getFrameCount()||next>=sprite.getFrameCount())return null;
      int[][] from=sprite.getFrameTextureData(current),to=sprite.getFrameTextureData(next);
      if(from==null||to==null)return null;
      if(state.pixels==null||state.pixels.length!=from.length)state.pixels=new int[from.length][];
      double weight=(double)tick/duration;
      for(int level=0;level<from.length;level++) {
        int[] a=from[level];if(a==null)return null;
        int[] b=level<to.length?to[level]:null;
        int[] out=state.pixels[level];
        if(out==null||out.length!=a.length)state.pixels[level]=out=new int[a.length];
        if(b==null||b.length!=a.length){System.arraycopy(a,0,out,0,a.length);continue;}
        for(int p=0;p<a.length;p++)out[p]=blend(a[p],b[p],weight);
      }
      return state.pixels;
    }catch(IllegalAccessException error){throw new IllegalStateException("Cannot interpolate atlas sprite",error);}
  }
  public static int blend(int current,int next,double weight) {
    double inverse=1-weight;
    int red=(int)(((current>>16)&255)*inverse+((next>>16)&255)*weight);
    int green=(int)(((current>>8)&255)*inverse+((next>>8)&255)*weight);
    int blue=(int)((current&255)*inverse+(next&255)*weight);
    // Vanilla interpolation keeps the current frame's alpha instead of fading cutout geometry.
    return current&0xff000000|red<<16|green<<8|blue;
  }
  public static void update(TextureAtlasSprite sprite) {
    int[][] pixels=frame(sprite);if(pixels==null)return;
    if(AngelicaUpload.upload(sprite,pixels))return;
    TextureUtil.uploadTextureMipmap(pixels,sprite.getIconWidth(),sprite.getIconHeight(),sprite.getOriginX(),sprite.getOriginY(),false,false);
  }
  private static final class AngelicaUpload {
    static final Method gutter,upload;
    static {
      Method g=null,u=null;
      try {
        Class<?> api=Class.forName("com.gtnewhorizons.angelica.rendering.celeritas.SpriteExtension");
        g=api.getMethod("angelica$getGutterWidth");
        u=Class.forName("com.gtnewhorizons.angelica.utils.SpritePadding").getMethod("uploadPadded",int[][].class,int.class,int.class,int.class,int.class,int.class,boolean.class,boolean.class);
      }catch(ClassNotFoundException absent) {
        // Ordinary Minecraft/OptiFine atlas.
      }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot bind interpolated sprite padding",error);}
      gutter=g;upload=u;
    }
    static boolean upload(TextureAtlasSprite sprite,int[][] pixels) {
      if(upload==null||!gutter.getDeclaringClass().isInstance(sprite))return false;
      try {
        upload.invoke(null,pixels,sprite.getIconWidth(),sprite.getIconHeight(),sprite.getOriginX(),sprite.getOriginY(),(Integer)gutter.invoke(sprite),false,false);
        return true;
      }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot upload interpolated sprite",error);}
    }
  }
}
