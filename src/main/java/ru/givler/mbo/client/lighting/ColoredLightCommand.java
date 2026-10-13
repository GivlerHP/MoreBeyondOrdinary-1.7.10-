package ru.givler.mbo.client.lighting;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import ru.givler.mbo.config.ColoredLightSources;

public final class ColoredLightCommand extends CommandBase {
  @Override public String getCommandName() { return "mbolight"; }
  @Override public String getCommandUsage(ICommandSender sender) { return "/mbolight reload | stats | inspect"; }
  @Override public int getRequiredPermissionLevel() { return 0; }
  @Override public void processCommand(ICommandSender sender, String[] args) {
    if (args.length == 1 && args[0].equals("inspect")) {
      net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
      net.minecraft.util.MovingObjectPosition hit = mc.objectMouseOver;
      if (hit != null && hit.entityHit != null) sender.addChatMessage(new ChatComponentText("Entity: " + ru.givler.mbo.lighting.ColoredLightApi.entityId(hit.entityHit)));
      else if (hit != null && hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld != null) {
        net.minecraft.block.Block block = mc.theWorld.getBlock(hit.blockX, hit.blockY, hit.blockZ);
        sender.addChatMessage(new ChatComponentText("Block: " + net.minecraft.block.Block.blockRegistry.getNameForObject(block) + ":" + mc.theWorld.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ)
            + " vanilla=" + block.getLightValue(mc.theWorld, hit.blockX, hit.blockY, hit.blockZ)));
      }
      net.minecraft.item.ItemStack held = mc.thePlayer == null ? null : mc.thePlayer.getHeldItem();
      if (held != null) sender.addChatMessage(new ChatComponentText("Item: " + net.minecraft.item.Item.itemRegistry.getNameForObject(held.getItem()) + ":" + held.getItemDamage()
          + " auto=" + ColoredLightAuto.itemLevel(held, mc.thePlayer)));
      return;
    }
    if (args.length == 1 && args[0].equals("reload")) {
      ru.givler.mbo.config.ColoredLightConfig.load(ColoredLightSources.file().getParentFile().getParentFile());
      boolean loaded = ColoredLightSources.reload();
      if (loaded) { ColoredLightClient.INSTANCE.reloadInputs(); ColoredDynamicLights.INSTANCE.invalidate(); }
      sender.addChatMessage(new ChatComponentText(loaded ? "MBO RGB rules reloaded: " + ColoredLightSources.file() : "MBO RGB rules rejected; previous rules retained. Check the log."));
    } else sender.addChatMessage(new ChatComponentText("MBO RGB: " + ColoredLightClient.INSTANCE.statistics()
        + ", moving=" + ColoredDynamicLights.INSTANCE.sources().size() + ", shader=" + ColoredLightShaderBridge.active));
  }
}
