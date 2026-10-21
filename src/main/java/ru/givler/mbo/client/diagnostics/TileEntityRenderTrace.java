package ru.givler.mbo.client.diagnostics;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.io.*;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Opt-in, bounded render trace. Observes state; never changes render settings or consumes GL errors. */
public final class TileEntityRenderTrace {
  public static final TileEntityRenderTrace INSTANCE=new TileEntityRenderTrace();
  private static volatile boolean enabled;
  private static PrintWriter output;
  private static File file;
  private static long deadline,started,frame,sequence,bytes;
  private static int radius,nextToken;
  private static final int MAX_BYTES=128*1024*1024;
  private static final ThreadLocal<ArrayDeque<Scope>> SCOPES=ThreadLocal.withInitial(ArrayDeque::new);
  private static final Set<String> owners=new HashSet<String>();
  private static final FloatBuffer matrix=BufferUtils.createFloatBuffer(16);
  private TileEntityRenderTrace() {}

  public static synchronized File start(int seconds,int distance)throws IOException {
    stop();
    File dir=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-tesr");
    if(!dir.isDirectory() && !dir.mkdirs())throw new IOException("Cannot create "+dir);
    file=new File(dir,"trace-"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())+".log");
    output=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"),262144));
    radius=distance;started=System.nanoTime();deadline=started+seconds*1000000000L;
    frame=sequence=bytes=0;owners.clear();SCOPES.remove();enabled=true;
    line("START","seconds="+seconds+" radius="+radius+" java="+System.getProperty("java.version"));
    line("NOTE","GL snapshots use Angelica's logical state when its redirector is active. Draw submission does not prove visible pixels. No glGetError calls are made.");
    for(ModContainer mod:Loader.instance().getActiveModList())line("MOD",mod.getModId()+" version="+mod.getVersion()+" source="+mod.getSource());
    return file;
  }
  public static synchronized void stop() {
    if(output!=null){line("STOP","trace complete");enabled=false;output.flush();output.close();output=null;}
    enabled=false;SCOPES.remove();
  }
  public static boolean enabled(){return enabled;}
  public static String status(){return (enabled?"recording ":"stopped ")+(file==null?"":file.getAbsolutePath());}
  public static void mark(String text){if(enabled)line("MARK",text);}
  @SubscribeEvent public void tick(TickEvent.RenderTickEvent event) {
    if(!enabled)return;
    if(event.phase==TickEvent.Phase.START) {
      if(System.nanoTime()>=deadline || bytes>=MAX_BYTES){stop();return;}
      frame++;
    } else if(output!=null && frame%20==0)output.flush();
  }
  private static boolean nearby(TileEntity tile) {
    Entity view=Minecraft.getMinecraft().renderViewEntity;
    return tile!=null && view!=null && tile.getDistanceFrom(view.posX,view.posY,view.posZ)<(double)radius*radius;
  }
  public static int begin(TileEntity tile) {
    if(!enabled || !nearby(tile))return 0;
    try {
      if(!Minecraft.getMinecraft().func_152345_ab()){line("WRONG_THREAD",tile.getClass().getName());return 0;}
      Object renderer=TileEntityRendererDispatcher.instance.getSpecialRenderer(tile);
      owner(tile.getClass());if(renderer!=null)owner(renderer.getClass());
      Scope scope=new Scope(++nextToken,tile.getClass().getName()+"@"+tile.xCoord+","+tile.yCoord+","+tile.zCoord);
      SCOPES.get().addLast(scope);
      line("TESR_BEGIN","id="+scope.token+" tile="+scope.tile+" renderer="+(renderer==null?"null":renderer.getClass().getName())+" "+state());
      return scope.token;
    }catch(Throwable error){failure(error);return 0;}
  }
  public static void end(int token) {
    if(token==0)return;
    ArrayDeque<Scope> scopes=SCOPES.get();Scope scope=scopes.peekLast();
    if(scope==null)return;
    if(scope.token!=token){if(enabled)line("UNBALANCED","expected="+scope.token+" actual="+token);return;}
    try{if(enabled)line("TESR_END","id="+token+" draws="+scope.draws+" tile="+scope.tile+" "+state());}
    catch(Throwable error){failure(error);}finally{scopes.removeLast();}
  }
  /** Called only at real Angelica draw/list submission entry points, not for each vertex. */
  public static void draw(String operation,int count) {
    if(!enabled)return;
    Scope scope=SCOPES.get().peekLast();if(scope==null)return;
    try {scope.draws++;line("DRAW","id="+scope.token+" part="+scope.draws+" op="+operation+" count="+count+" "+state()+" caller="+caller());}
    catch(Throwable error){failure(error);}
  }
  public static void visible(List<TileEntity> before,List<TileEntity> after) {
    if(!enabled)return;
    try {
      Set<TileEntity> old=Collections.newSetFromMap(new IdentityHashMap<TileEntity,Boolean>());old.addAll(before);
      line("VISIBLE","angelica="+before.size()+" mbo="+after.size());
      for(TileEntity tile:after)if(nearby(tile))line("VISIBLE_TILE",tile.getClass().getName()+"@"+tile.xCoord+","+tile.yCoord+","+tile.zCoord+" added="+!old.contains(tile));
      Set<TileEntity> current=Collections.newSetFromMap(new IdentityHashMap<TileEntity,Boolean>());current.addAll(after);
      for(TileEntity tile:before)if(!current.contains(tile) && nearby(tile))line("DROPPED_TILE",tile.getClass().getName()+"@"+tile.xCoord+","+tile.yCoord+","+tile.zCoord);
    }catch(Throwable error){failure(error);}
  }
  public static void section(int x,int y,int z,int global,int culled,int prepared) {
    if(enabled)line("SECTION_UPLOAD",x+","+y+","+z+" global="+global+" culled="+culled+" prepared="+prepared);
  }
  public static void deferred(String phase) {
    if(!enabled)return;
    try{line("DEFERRED_"+phase,state());}catch(Throwable error){failure(error);}
  }
  public static void rebuild(int x1,int y1,int z1,int x2,int y2,int z2) {
    if(enabled)try{line("REBUILD",x1+","+y1+","+z1+".."+x2+","+y2+","+z2+" caller="+caller());}catch(Throwable error){failure(error);}
  }
  private static synchronized void owner(Class<?> type) {
    if(!owners.add(type.getName()))return;
    String source="unknown";
    try{if(type.getProtectionDomain().getCodeSource()!=null)source=String.valueOf(type.getProtectionDomain().getCodeSource().getLocation());}catch(SecurityException ignored){}
    line("OWNER",type.getName()+" source="+source);
  }
  private static String caller() {
    StringBuilder s=new StringBuilder();int count=0;
    for(StackTraceElement e:Thread.currentThread().getStackTrace()) {
      String name=e.getClassName();
      if(name.equals(Thread.class.getName()) || name.equals(TileEntityRenderTrace.class.getName()) || name.endsWith(".GLStateManager"))continue;
      if(count++==8)break;
      if(s.length()!=0)s.append(" <- ");s.append(e);
    }
    return s.toString();
  }
  private static String state() {
    int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    StringBuilder s=new StringBuilder("program=").append(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM));
    s.append(" depth=").append(GL11.glIsEnabled(GL11.GL_DEPTH_TEST)).append('/').append(GL11.glGetInteger(GL11.GL_DEPTH_FUNC)).append('/').append(GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK));
    s.append(" alpha=").append(GL11.glIsEnabled(GL11.GL_ALPHA_TEST)).append('/').append(GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC)).append('/').append(GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF));
    s.append(" blend=").append(GL11.glIsEnabled(GL11.GL_BLEND)).append(" cull=").append(GL11.glIsEnabled(GL11.GL_CULL_FACE));
    s.append(" vao=").append(GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)).append(" vbo=").append(GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING));
    s.append(" fbo=").append(GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)).append(" active=").append(active-GL13.GL_TEXTURE0);
    try {
      for(int unit=0;unit<3;unit++) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0+unit);
        s.append(" tex").append(unit).append('=').append(GL11.glIsEnabled(GL11.GL_TEXTURE_2D)).append('/').append(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
      }
    }finally{GL13.glActiveTexture(active);}
    matrix.clear();GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,matrix);int hash=1;
    for(int i=0;i<16;i++)hash=31*hash+Float.floatToIntBits(matrix.get(i));
    s.append(" modelviewHash=").append(hash);
    return s.toString();
  }
  private static synchronized void line(String kind,String text) {
    if(output==null || bytes>=MAX_BYTES)return;
    String entry=(++sequence)+" ns="+(System.nanoTime()-started)+" frame="+frame+" thread="+Thread.currentThread().getName()+" "+kind+" "+text.replace('\n',' ').replace('\r',' ');
    output.println(entry);bytes+=entry.length()*2L;
  }
  private static void failure(Throwable error){line("TRACE_ERROR",error.toString());}
  private static final class Scope {final int token;final String tile;int draws;Scope(int token,String tile){this.token=token;this.tile=tile;}}
}
