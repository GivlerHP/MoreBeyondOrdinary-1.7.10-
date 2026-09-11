package ru.givler.mbo.network;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.item.ItemAreaEditor;

public final class EditorPacketAccess {
  private static final org.apache.logging.log4j.Logger LOGGER=org.apache.logging.log4j.LogManager.getLogger("MBO.NetworkTasks");
  private static final int MAX_QUEUED=8192;
  private static final java.util.Queue<Runnable> SERVER_TASKS =
      new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
  private static final java.util.Queue<Runnable> CLIENT_TASKS =
      new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
  private static final java.util.concurrent.atomic.AtomicInteger SERVER_COUNT=new java.util.concurrent.atomic.AtomicInteger();
  private static final java.util.concurrent.atomic.AtomicInteger CLIENT_COUNT=new java.util.concurrent.atomic.AtomicInteger();
  private static final java.util.concurrent.atomic.AtomicLong LAST_OVERFLOW_WARNING=new java.util.concurrent.atomic.AtomicLong();

  private EditorPacketAccess() {}

  public static void schedule(MessageContext context, Runnable action) {
    offer(SERVER_TASKS,SERVER_COUNT,action,"server");
  }

  public static void scheduleClient(Runnable action) {
    offer(CLIENT_TASKS,CLIENT_COUNT,action,"client");
  }

  static void drainServer() {
    drain(SERVER_TASKS,SERVER_COUNT,"server");
  }

  static void drainClient() {
    drain(CLIENT_TASKS,CLIENT_COUNT,"client");
  }

  private static void offer(java.util.Queue<Runnable> queue,java.util.concurrent.atomic.AtomicInteger count,Runnable task,String side){if(task==null)return;int queued=count.incrementAndGet();if(queued>MAX_QUEUED){count.decrementAndGet();long now=System.currentTimeMillis(),last=LAST_OVERFLOW_WARNING.get();if(now-last>=5000L&&LAST_OVERFLOW_WARNING.compareAndSet(last,now))LOGGER.warn("Dropping {} network tasks: queue limit {} reached",side,MAX_QUEUED);return;}queue.offer(task);}

  private static void drain(java.util.Queue<Runnable> queue,java.util.concurrent.atomic.AtomicInteger count,String side) {
    Runnable task;
    int handled = 0;
    while (handled < 10000 && (task = queue.poll()) != null) {
      count.decrementAndGet();
      handled++;
      try{task.run();}catch(Exception error){LOGGER.error("Failed to execute queued "+side+" network task",error);}
    }
  }

  public static ItemStack heldEditor(EntityPlayerMP player) {
    ItemStack held = player == null ? null : player.getCurrentEquippedItem();
    return held != null
            && held.getItem() instanceof ItemAreaEditor
            && BuilderAccess.canUseTool(player, held.getItem())
        ? held
        : null;
  }
}
