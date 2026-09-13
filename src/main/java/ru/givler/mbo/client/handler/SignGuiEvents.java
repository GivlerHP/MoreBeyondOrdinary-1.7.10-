package ru.givler.mbo.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.client.event.GuiOpenEvent;
import ru.givler.mbo.client.gui.GuiSign;

public final class SignGuiEvents {
  private static final Field SIGN_FIELD =
      ReflectionHelper.findField(GuiEditSign.class, "tileSign", "field_146848_f");

  @SubscribeEvent
  public void onGuiOpen(GuiOpenEvent event) {
    if (!(event.gui instanceof GuiEditSign)) return;
    try {
      event.gui = new GuiSign((TileEntitySign) SIGN_FIELD.get(event.gui));
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Could not open the MBO sign editor", exception);
    }
  }
}
