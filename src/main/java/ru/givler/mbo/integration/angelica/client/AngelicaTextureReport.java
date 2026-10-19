package ru.givler.mbo.integration.angelica.client;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Inspect the renderer's actual parsed bindings; never rewrites resource-pack files. */
public final class AngelicaTextureReport extends CommandBase {
  @Override public String getCommandName(){return "mboctm";}
  @Override public String getCommandUsage(ICommandSender sender){return "/mboctm";}
  @Override public int getRequiredPermissionLevel(){return 0;}
  @Override public void processCommand(ICommandSender sender,String[] args) {
    try {
      Class<?> api=Class.forName("com.prupe.mcpatcher.ctm.CTMUtils");
      Field field=api.getDeclaredField("overrides");field.setAccessible(true);Object state=field.get(null);
      if(state==null){sender.addChatMessage(new ChatComponentText("MBO CTM: Angelica has no loaded rule set"));return;}
      List<?> all=(List<?>)state.getClass().getMethod("getAll").invoke(state);
      Map<?,?> blocks=(Map<?,?>)state.getClass().getMethod("getBlock").invoke(state);
      Map<?,?> tiles=(Map<?,?>)state.getClass().getMethod("getTile").invoke(state);
      File output=new File(Minecraft.getMinecraft().mcDataDir,"logs/mbo-ctm-report.txt");
      File parent=output.getParentFile();if(!parent.isDirectory() && !parent.mkdirs())throw new java.io.IOException("Cannot create "+parent);
      try(PrintWriter report=new PrintWriter(output,"UTF-8")) {
        report.println("Angelica CTM: "+all.size()+" parsed rules; "+blocks.size()+" block bindings; "+tiles.size()+" tile bindings");
        report.println("Numeric IDs below are from this running registry. Resource-pack files are unchanged.");
        for(Object rule:all)report.println("RULE "+rule);
        for(Map.Entry<?,?> entry:blocks.entrySet()) {
          Block block=(Block)entry.getKey();report.println("BLOCK "+Block.getIdFromBlock(block)+" = "+Block.blockRegistry.getNameForObject(block)+" "+entry.getValue());
        }
        for(Map.Entry<?,?> entry:tiles.entrySet())report.println("TILE "+entry.getKey()+" "+entry.getValue());
      }
      sender.addChatMessage(new ChatComponentText("MBO CTM: "+all.size()+" rules, report: "+output.getPath()));
    } catch(ReflectiveOperationException | java.io.IOException error) {
      sender.addChatMessage(new ChatComponentText("MBO CTM report failed: "+error));
    }
  }
}
