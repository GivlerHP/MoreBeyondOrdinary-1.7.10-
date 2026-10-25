package ru.givler.mbo.client.diagnostics;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.command.*;
import net.minecraft.util.*;

/** Opt-in, bounded capture of the selected block's actual emitted vertex colors. */
public final class BlockColorProbe extends CommandBase {
  private static volatile Target target;
  private static final class Target {
    final int x,y,z; final long expires=System.nanoTime()+30000000000L;
    int remaining=8;
    Target(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
  }
  private static Field index,count,buffer;
  private static java.lang.reflect.Method tessellator;
  public String getCommandName(){return "mbocolor";}
  public String getCommandUsage(ICommandSender sender){return "/mbocolor [x y z|stop]";}
  public int getRequiredPermissionLevel(){return 0;}
  public void processCommand(ICommandSender sender,String[] args) {
    try {
      if(args.length==1 && args[0].equals("stop")){target=null;return;}
      Minecraft mc=Minecraft.getMinecraft();
      if(mc.theWorld==null)throw new IllegalStateException("No world");
      int x,y,z;
      if(args.length==3){x=Integer.parseInt(args[0]);y=Integer.parseInt(args[1]);z=Integer.parseInt(args[2]);}
      else if(args.length==0 && mc.objectMouseOver!=null && mc.objectMouseOver.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK){
        x=mc.objectMouseOver.blockX;y=mc.objectMouseOver.blockY;z=mc.objectMouseOver.blockZ;
      }else throw new IllegalArgumentException(getCommandUsage(sender));
      Block block=mc.theWorld.getBlock(x,y,z);int meta=mc.theWorld.getBlockMetadata(x,y,z);
      int color=block.colorMultiplier(mc.theWorld,x,y,z);
      System.out.println("[MBO color probe] target="+Block.blockRegistry.getNameForObject(block)+" class="+block.getClass().getName()+" pos="+x+","+y+","+z+" meta="+meta+" color="+String.format("%06X",color & 0xffffff)+" renderType="+block.getRenderType()+" shaders="+ru.givler.mbo.client.render.ShaderPackState.isActive());
      target=new Target(x,y,z);
      mc.theWorld.markBlockRangeForRenderUpdate(x-1,y-1,z-1,x+1,y+1,z+1);
      sender.addChatMessage(new ChatComponentText("MBO color probe: "+x+" "+y+" "+z+"; result in logs/fml-client-latest.log"));
    }catch(Exception error){sender.addChatMessage(new ChatComponentText("MBO color probe: "+error));}
  }
  public static long begin(int x,int y,int z) {
    Target selected=target;
    if(selected==null || selected.x!=x || selected.y!=y || selected.z!=z || System.nanoTime()>selected.expires)return -1;
    synchronized(selected){if(selected.remaining--<=0)return -1;}
    try {
      if(index==null){
        index=ReflectionHelper.findField(Tessellator.class,"rawBufferIndex","field_147569_p");
        count=ReflectionHelper.findField(Tessellator.class,"vertexCount","field_78406_i");
        buffer=ReflectionHelper.findField(Tessellator.class,"rawBuffer","field_78405_h");
        tessellator=Class.forName("com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager").getMethod("get");
      }
      Object t=tessellator.invoke(null);
      return ((long)index.getInt(t)<<32)|(count.getInt(t)&0xffffffffL);
    }catch(Exception error){target=null;System.out.println("[MBO color probe] failed: "+error);return -1;}
  }
  public static void end(long token,Block block,int pass,boolean drawn) {
    if(token<0)return;
    try {
      Object t=tessellator.invoke(null);int start=(int)(token>>>32),first=(int)token;
      int length=index.getInt(t)-start,vertices=count.getInt(t)-first;
      int stride=vertices>0 && length%vertices==0?length/vertices:-1;
      Set<String> colors=new LinkedHashSet<String>();int[] raw=(int[])buffer.get(t);
      if(stride>=8)for(int i=start;i<start+length && colors.size()<24;i+=stride)colors.add(String.format("%08X",raw[i+5]));
      System.out.println("[MBO color probe] mesh="+Block.blockRegistry.getNameForObject(block)+" pass="+pass+" drawn="+drawn+" vertices="+vertices+" stride="+stride+" packedColors="+colors+" endian="+java.nio.ByteOrder.nativeOrder()+" thread="+Thread.currentThread().getName());
    }catch(Exception error){System.out.println("[MBO color probe] capture failed: "+error);}
  }
}
