package ru.givler.mbo.client.diagnostics;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import net.minecraft.block.Block;
/** Bounded opt-in attribution of actual renderer calls, not their wrapper name. */
public final class ChunkRenderCostProbe extends net.minecraft.command.CommandBase {
  private static volatile long deadline;
  private static final ConcurrentHashMap<String,AtomicLong> nanos=new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String,AtomicLong> calls=new ConcurrentHashMap<>();
  private static final AtomicBoolean reported=new AtomicBoolean(true);
  public String getCommandName(){return "mbomeshcost";}
  public String getCommandUsage(net.minecraft.command.ICommandSender s){return "/mbomeshcost";}
  public int getRequiredPermissionLevel(){return 0;}
  public void processCommand(net.minecraft.command.ICommandSender sender,String[] args){
    deadline=0;nanos.clear();calls.clear();reported.set(false);deadline=System.nanoTime()+30000000000L;
    sender.addChatMessage(new net.minecraft.util.ChatComponentText("MBO mesh renderer measurement: 30 seconds; results in client log."));
  }
  public static long begin(){return deadline==0?0:System.nanoTime();}
  public static void end(long start,Block block,int pass){
    if(start==0)return;
    long now=System.nanoTime();
    if(now>deadline){
      if(reported.compareAndSet(false,true)) {
        deadline=0;
        List<Map.Entry<String,AtomicLong>> sorted=new ArrayList<>(nanos.entrySet());
        sorted.sort((a,b)->Long.compare(b.getValue().get(),a.getValue().get()));
        System.out.println("[MBO mesh cost] summed CPU-wall spans across rendering threads; not frame time");
        for(int i=0;i<Math.min(20,sorted.size());i++){
          Map.Entry<String,AtomicLong> row=sorted.get(i);
          System.out.println("[MBO mesh cost] "+row.getKey()+" ms="+row.getValue().get()/1000000.0+" calls="+calls.get(row.getKey()).get());
        }
      }
      return;
    }
    String key=block.getClass().getName()+" pass="+pass+" thread="+Thread.currentThread().getName();
    calls.computeIfAbsent(key,k->new AtomicLong()).incrementAndGet();
    nanos.computeIfAbsent(key,k->new AtomicLong()).addAndGet(now-start);
  }
}
