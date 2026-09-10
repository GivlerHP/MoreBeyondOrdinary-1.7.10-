package ru.givler.mbo.network;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.item.ItemAreaEditor;

public final class EditorPacketAccess {
    private static final java.util.Queue<Runnable> SERVER_TASKS=new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    private static final java.util.Queue<Runnable> CLIENT_TASKS=new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    private EditorPacketAccess(){}
    public static void schedule(MessageContext context,Runnable action){if(action!=null)SERVER_TASKS.offer(action);}
    public static void scheduleClient(Runnable action){if(action!=null)CLIENT_TASKS.offer(action);}
    static void drainServer(){drain(SERVER_TASKS);}
    static void drainClient(){drain(CLIENT_TASKS);}
    private static void drain(java.util.Queue<Runnable> queue){Runnable task;int handled=0;while(handled<10000&&(task=queue.poll())!=null){handled++;task.run();}}
    public static ItemStack heldEditor(EntityPlayerMP player){ItemStack held=player==null?null:player.getCurrentEquippedItem();return held!=null&&held.getItem() instanceof ItemAreaEditor&&BuilderAccess.canUseTool(player,held.getItem())?held:null;}
}
