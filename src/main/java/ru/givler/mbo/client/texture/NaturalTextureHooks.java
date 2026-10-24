package ru.givler.mbo.client.texture;

import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** The E7 natural.properties UV feature missing from Angelica's block-whitelist randomizer. */
public final class NaturalTextureHooks {
  public static final NaturalTextureHooks INSTANCE=new NaturalTextureHooks();
  private static volatile Map<String,Integer> rules=Collections.emptyMap();
  private static volatile boolean configured;
  private static BooleanSupplier enabled=()->false;
  private static final ThreadLocal<State> STATE=new ThreadLocal<State>() {protected State initialValue(){return new State();}};
  private static final class State {int depth;Frame[] frames=new Frame[8];}
  private static final class Frame {boolean active,flip;int rotation;double u0,v0,du,dv;}
  private NaturalTextureHooks() {}
  public static void configure(BooleanSupplier setting){enabled=setting;}

  @SubscribeEvent public void textures(TextureStitchEvent.Post event) {
    if(event.map.getTextureType()!=0)return;
    Map<String,Integer> next=Collections.emptyMap();boolean found=false;
    for(String path:new String[]{"optifine/natural.properties","mcpatcher/natural.properties"}) {
      try(InputStream in=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(path)).getInputStream()) {
        next=parse(new InputStreamReader(in,"UTF-8"));found=true;break;
      }catch(FileNotFoundException absent){}catch(IOException error){System.err.println("[MBO] Natural textures: "+error);}
    }
    publish(next,found);
    System.out.println("[MBO] Natural textures: "+rules.size()+" texture rules; OptiFine resource configuration="+configured);
  }
  static void publish(Map<String,Integer> next,boolean found) {
    rules=Collections.unmodifiableMap(new HashMap<String,Integer>(next));configured=found;
  }
  public static Map<String,Integer> parse(Reader reader)throws IOException {
    Map<String,Integer> result=new HashMap<String,Integer>();BufferedReader lines=new BufferedReader(reader);String line;
    while((line=lines.readLine())!=null) {
      line=line.replace("\uFEFF","").trim();if(line.isEmpty() || line.startsWith("#"))continue;
      int equals=line.indexOf('=');if(equals<1)continue;
      String key=line.substring(0,equals).trim(),value=line.substring(equals+1).trim();int mode;
      if(value.equals("4"))mode=4;else if(value.equals("2"))mode=2;else if(value.equals("F"))mode=8;
      else if(value.equals("4F"))mode=12;else if(value.equals("2F"))mode=10;
      else {System.err.println("[MBO] Invalid natural texture value: "+key+"="+value);continue;}
      result.put(normalize(key),mode);
    }
    return result;
  }
  static String normalize(String key) {
    if(key.endsWith(".png"))key=key.substring(0,key.length()-4);
    if(key.startsWith("textures/blocks/"))key=key.substring(16);
    return key.indexOf(':')<0?"minecraft:"+key:key;
  }
  public static boolean allowBuiltin(boolean original){return original && !configured && enabled.getAsBoolean();}

  public static long begin(RenderBlocks renderer,Block block,double x,double y,double z,IIcon icon,int face) {
    return beginWorld(renderer==null?null:renderer.blockAccess,x,y,z,icon,face,renderer!=null && renderer.hasOverrideBlockTexture());
  }
  public static long beginWorld(net.minecraft.world.IBlockAccess world,double x,double y,double z,IIcon icon,int face,boolean override) {
    State state=STATE.get();Map<String,Integer> current=rules;
    if(current.isEmpty() && state.depth==0)return -1;
    int previous=state.depth;
    if(state.depth==state.frames.length)state.frames=Arrays.copyOf(state.frames,state.depth*2);
    Frame frame=state.frames[state.depth];if(frame==null)state.frames[state.depth]=frame=new Frame();state.depth++;frame.active=false;
    if(world==null || icon==null || override || face<0 || face>5 || !enabled.getAsBoolean())return previous;
    Integer mode=current.get(normalize(icon.getIconName()));if(mode==null)return previous;
    int random=random((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z),face);
    frame.rotation=(mode&4)!=0?random&3:(mode&2)!=0?random&2:0;
    frame.flip=(mode&8)!=0 && (random&4)!=0;
    frame.u0=icon.getMinU();frame.v0=icon.getMinV();frame.du=icon.getMaxU()-frame.u0;frame.dv=icon.getMaxV()-frame.v0;
    frame.active=frame.du>0 && frame.dv>0;
    return previous;
  }
  public static void end(long previous){if(previous>=0)STATE.get().depth=(int)previous;}
  public static double u(double u,double v){return uv(u,v,true);}
  public static double v(double u,double v){return uv(u,v,false);}
  private static double uv(double u,double v,boolean first) {
    State state=STATE.get();if(state.depth==0)return first?u:v;
    Frame f=state.frames[state.depth-1];if(!f.active)return first?u:v;
    double a=(u-f.u0)/f.du,b=(v-f.v0)/f.dv,ru=a,rv=b;
    switch(f.rotation){case 1:ru=b;rv=1-a;break;case 2:ru=1-a;rv=1-b;break;case 3:ru=1-b;rv=a;break;default:break;}
    if(f.flip)ru=1-ru;
    return first?f.u0+ru*f.du:f.v0+rv*f.dv;
  }
  private static int hash(int n){n=(n^61)^(n>>16);n+=n<<3;n^=n>>4;n*=668265261;n^=n>>15;return n;}
  public static int random(int x,int y,int z,int face){return hash(hash(hash(hash(face+37)+x)+z)+y);}
}
