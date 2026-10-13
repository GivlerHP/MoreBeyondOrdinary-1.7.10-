package ru.givler.mbo.client.lighting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.config.ColoredLightConfig;

/** Forge GUI events work with both the vanilla and E7 video settings screen. */
public final class ColoredLightMenu {
  @SubscribeEvent public void init(GuiScreenEvent.InitGuiEvent.Post event) {
    if (!(event.gui instanceof GuiVideoSettings)) return;
    int x = Math.min(event.gui.width - 24,event.gui.width / 2 + 160);
    int y = event.gui.width < 360 ? 6 : Math.max(28,event.gui.height / 6 - 10);
    event.buttonList.add(new TorchButton(x,y));
  }
  @SubscribeEvent public void click(GuiScreenEvent.ActionPerformedEvent.Pre event) {
    if (!(event.button instanceof TorchButton)) return;
    event.setCanceled(true);
    Minecraft.getMinecraft().displayGuiScreen(new Options(event.gui));
  }
  public static final class TorchButton extends GuiButton {
    private final RenderItem renderer = new RenderItem();
    private final ItemStack icon = new ItemStack(Blocks.torch);
    TorchButton(int x,int y) { super(29471,x,y,20,20,""); }
    @Override public void drawButton(Minecraft mc,int mouseX,int mouseY) {
      super.drawButton(mc,mouseX,mouseY);
      if (!visible) return;
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
      try { RenderHelper.enableGUIStandardItemLighting(); renderer.renderItemAndEffectIntoGUI(mc.fontRenderer,mc.getTextureManager(),icon,xPosition+2,yPosition+2); }
      finally { GL11.glPopAttrib(); }
      if (mouseX >= xPosition && mouseX < xPosition+width && mouseY >= yPosition && mouseY < yPosition+height) {
        String label = I18n.format("mbo.light.title");
        mc.fontRenderer.drawStringWithShadow(label,Math.max(4,xPosition-mc.fontRenderer.getStringWidth(label)+20),yPosition+24,0xFFFFFF);
      }
    }
  }
  public static final class Options extends GuiScreen {
    private final GuiScreen parent;
    Options(GuiScreen parent) { this.parent = parent; }
    @Override public void initGui() {
      buttonList.clear();
      buttonList.add(new GuiButton(0,width/2-100,height/2-64,200,20,toggleLabel()));
      buttonList.add(new GuiButton(1,width/2-100,height/2-38,200,20,valueLabel("mbo.light.saturation",ColoredLightConfig.strength)));
      buttonList.add(new GuiButton(2,width/2-100,height/2-12,200,20,valueLabel("mbo.light.brightness",ColoredLightConfig.brightnessGain)));
      buttonList.add(new GuiButton(4,width/2-100,height/2+14,200,20,I18n.format("mbo.light.entities")+": "+I18n.format(ColoredLightConfig.entityLighting?"options.on":"options.off")));
      buttonList.add(new GuiButton(3,width/2-100,height/2+66,200,20,I18n.format("gui.done")));
    }
    private static String toggleLabel() { return I18n.format("mbo.light.enabled") + ": " + I18n.format(ColoredLightConfig.enabled ? "options.on" : "options.off"); }
    private static String valueLabel(String key,float value) { return I18n.format(key) + ": " + Math.round(value*100) + "%"; }
    @Override protected void actionPerformed(GuiButton button) {
      if (button.id == 3) { mc.displayGuiScreen(parent); return; }
      if (button.id == 4) {
        ColoredLightConfig.entityLighting=!ColoredLightConfig.entityLighting;
        if(!ColoredLightConfig.entityLighting)ColoredEntityLightHooks.release();
        ColoredLightConfig.saveUserOptions();initGui();return;
      }
      if (button.id == 0) ColoredLightConfig.enabled = !ColoredLightConfig.enabled;
      if (button.id == 1) ColoredLightConfig.strength = next(ColoredLightConfig.strength,new float[]{0.25F,0.5F,0.75F,1F});
      if (button.id == 2) ColoredLightConfig.brightnessGain = next(ColoredLightConfig.brightnessGain,new float[]{0.5F,1F,1.25F,1.5F,2F});
      ColoredLightConfig.saveUserOptions(); ColoredLightClient.INSTANCE.applyOptions(); initGui();
    }
    private static float next(float value,float[] values) { for (float candidate : values) if (candidate > value + 0.001F) return candidate; return values[0]; }
    @Override protected void keyTyped(char letter,int key) { if (key == 1) mc.displayGuiScreen(parent); }
    @Override public void drawScreen(int mouseX,int mouseY,float partial) {
      drawDefaultBackground(); drawCenteredString(fontRendererObj,I18n.format("mbo.light.title"),width/2,28,0xFFFFFF);
      drawCenteredString(fontRendererObj,I18n.format("mbo.light.offHint"),width/2,height/2+44,0xAAAAAA);
      super.drawScreen(mouseX,mouseY,partial);
    }
  }
}
