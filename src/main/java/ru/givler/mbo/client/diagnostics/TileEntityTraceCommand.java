package ru.givler.mbo.client.diagnostics;

import net.minecraft.command.*;
import net.minecraft.util.ChatComponentText;

public final class TileEntityTraceCommand extends CommandBase {
  public String getCommandName(){return "mbotesrdebug";}
  public String getCommandUsage(ICommandSender sender){return "/mbotesrdebug start [seconds:1-120] [radius:1-128] | stop | status | mark [text]";}
  public int getRequiredPermissionLevel(){return 0;}
  public void processCommand(ICommandSender sender,String[] args) {
    try {
      String action=args.length==0?"status":args[0];
      if(action.equals("start")) {
        int seconds=args.length>1?Integer.parseInt(args[1]):30;
        int radius=args.length>2?Integer.parseInt(args[2]):32;
        if(seconds<1 || seconds>120 || radius<1 || radius>128)throw new IllegalArgumentException(getCommandUsage(sender));
        sender.addChatMessage(new ChatComponentText("MBO TESR trace: "+TileEntityRenderTrace.start(seconds,radius)+" (FPS may drop during recording)"));
      } else if(action.equals("stop")){TileEntityRenderTrace.stop();sender.addChatMessage(new ChatComponentText(TileEntityRenderTrace.status()));}
      else if(action.equals("status"))sender.addChatMessage(new ChatComponentText(TileEntityRenderTrace.status()));
      else if(action.equals("mark")) {
        StringBuilder text=new StringBuilder();for(int i=1;i<args.length;i++)text.append(args[i]).append(' ');
        TileEntityRenderTrace.mark(text.length()==0?"visual flicker":text.toString());
        sender.addChatMessage(new ChatComponentText("MBO TESR trace: marker added"));
      } else throw new IllegalArgumentException(getCommandUsage(sender));
    } catch(Exception error){sender.addChatMessage(new ChatComponentText("MBO TESR trace: "+error));}
  }
}
