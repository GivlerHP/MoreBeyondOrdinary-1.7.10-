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
      buttonList.add(new LightSlider(1,width/2-100,height/2-38,1F,ColoredLightConfig.strength));
      buttonList.add(new LightSlider(2,width/2-100,height/2-12,2F,ColoredLightConfig.brightnessGain));
      buttonList.add(new GuiButton(4,width/2-100,height/2+14,200,20,I18n.format("mbo.light.entities")+": "+I18n.format(ColoredLightConfig.entityLighting?"options.on":"options.off")));
      buttonList.add(new GuiButton(3,width/2-100,height/2+66,200,20,I18n.format("gui.done")));
    }
    private static String toggleLabel() { return I18n.format("mbo.light.enabled") + ": " + I18n.format(ColoredLightConfig.enabled ? "options.on" : "options.off"); }
    private static String valueLabel(String key,float value) { return I18n.format(key) + ": " + Math.round(value*100) + "%"; }
    @Override protected void actionPerformed(GuiButton button) {
      if(button instanceof LightSlider)return;
      if (button.id == 3) { mc.displayGuiScreen(parent); return; }
      if (button.id == 4) {
        ColoredLightConfig.entityLighting=!ColoredLightConfig.entityLighting;
        if(!ColoredLightConfig.entityLighting)ColoredEntityLightHooks.release();
        ColoredLightConfig.saveUserOptions();initGui();return;
      }
      if (button.id == 0) ColoredLightConfig.enabled = !ColoredLightConfig.enabled;
      ColoredLightConfig.saveUserOptions(); ColoredLightClient.INSTANCE.applyOptions(); initGui();
    }
    @Override public void onGuiClosed() {
      for(Object button:buttonList)if(button instanceof LightSlider)((LightSlider)button).commit();
    }
    @Override protected void keyTyped(char letter,int key) { if (key == 1) mc.displayGuiScreen(parent); }
    @Override public void drawScreen(int mouseX,int mouseY,float partial) {
      drawDefaultBackground(); drawCenteredString(fontRendererObj,I18n.format("mbo.light.title"),width/2,28,0xFFFFFF);
      drawCenteredString(fontRendererObj,I18n.format("mbo.light.offHint"),width/2,height/2+44,0xAAAAAA);
      super.drawScreen(mouseX,mouseY,partial);
    }
  }
  public static final class LightSlider extends GuiButton {
    private final float maximum;
    private float value;
    private boolean dragging,changed;
    LightSlider(int id,int x,int y,float maximum,float initial) {
      super(id,x,y,200,20,"");this.maximum=maximum;value=Math.max(0,Math.min(maximum,initial));label();
    }
    static float mouseValue(int mouseX,int x,int width,float maximum) {
      float fraction=Math.max(0,Math.min(1,(mouseX-x-4)/(float)(width-8)));
      return Math.round(fraction*maximum*100)/100F;
    }
    private void label() {displayString=Options.valueLabel(id==1?"mbo.light.saturation":"mbo.light.brightness",value);}
    private void update(int mouseX) {
      float next=mouseValue(mouseX,xPosition,width,maximum);
      if(next!=value){value=next;changed=true;label();}
    }
    @Override public int getHoverState(boolean hovered) {return 0;}
    @Override public boolean mousePressed(Minecraft mc,int mouseX,int mouseY) {
      if(!super.mousePressed(mc,mouseX,mouseY))return false;
      dragging=true;update(mouseX);return true;
    }
    @Override protected void mouseDragged(Minecraft mc,int mouseX,int mouseY) {
      if(!visible)return;
      if(dragging)update(mouseX);
      GL11.glColor4f(1,1,1,1);
      int handle=xPosition+Math.round((width-8)*value/maximum);
      drawTexturedModalRect(handle,yPosition,0,66,4,20);
      drawTexturedModalRect(handle+4,yPosition,196,66,4,20);
    }
    @Override public void mouseReleased(int mouseX,int mouseY) {dragging=false;commit();}
    private void commit() {
      if(!changed)return;changed=false;
      if(id==1)ColoredLightConfig.strength=value;else ColoredLightConfig.brightnessGain=value;
      ColoredLightConfig.saveUserOptions();ColoredLightClient.INSTANCE.applyOptions();
    }
  }
}
