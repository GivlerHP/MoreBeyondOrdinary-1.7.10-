package ru.givler.mbo.client.settings;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;

public final class WindowSettingsMenu {
  @SubscribeEvent public void init(GuiScreenEvent.InitGuiEvent.Post event) {
    if(!(event.gui instanceof GuiOptions))return;
    event.buttonList.add(new WindowButton(event.gui.width/2-155,event.gui.height/6+42));
  }
  @SubscribeEvent public void click(GuiScreenEvent.ActionPerformedEvent.Pre event) {
    if(!(event.button instanceof WindowButton))return;
    event.setCanceled(true);
    Minecraft.getMinecraft().displayGuiScreen(new Screen(event.gui));
  }
  @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
    if(event.phase==TickEvent.Phase.END)WindowSettings.tick();
  }
  private static final class WindowButton extends GuiButton {
    WindowButton(int x,int y){super(29431,x,y,150,20,I18n.format("mbo.window.title"));}
  }
  public static final class Screen extends GuiScreen {
    private final GuiScreen parent;
    private boolean pause,fullscreen,borderless;
    public Screen(GuiScreen parent) {
      this.parent=parent;pause=WindowSettings.pause();fullscreen=WindowSettings.fullscreen();borderless=WindowSettings.borderless();
    }
    @Override public void initGui() {
      buttonList.clear();int y=height/2-65;
      buttonList.add(new GuiButton(0,width/2-150,y,300,20,label("fullscreen",fullscreen)));
      GuiButton border=new GuiButton(1,width/2-150,y+24,300,20,label("borderless",borderless));border.enabled=WindowSettings.supportsBorderless();buttonList.add(border);
      buttonList.add(new GuiButton(2,width/2-150,y+48,300,20,label("pause",pause)));
      buttonList.add(new GuiButton(3,width/2-150,y+110,145,20,I18n.format("mbo.window.apply")));
      buttonList.add(new GuiButton(4,width/2+5,y+110,145,20,I18n.format("gui.cancel")));
    }
    private String label(String key,boolean value){return I18n.format("mbo.window."+key)+": "+I18n.format(value?"options.on":"options.off");}
    @Override protected void actionPerformed(GuiButton button) {
      if(button.id==0)fullscreen=!fullscreen;
      if(button.id==1)borderless=!borderless;
      if(button.id==2)pause=!pause;
      if(button.id==3){WindowSettings.apply(pause,fullscreen,borderless);mc.displayGuiScreen(parent);return;}
      if(button.id==4){mc.displayGuiScreen(parent);return;}
      initGui();
    }
    @Override protected void keyTyped(char letter,int key){if(key==1)mc.displayGuiScreen(parent);}
    @Override public void drawScreen(int x,int y,float partial) {
      drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("mbo.window.title"),width/2,height/2-95,0xFFFFFF);
      String hint=WindowSettings.supportsBorderless()?"mbo.window.pause.hint":"mbo.window.unavailable";
      if(y>=height/2-41 && y<height/2-21)hint="mbo.window.borderless.hint";
      java.util.List<String> lines=fontRendererObj.listFormattedStringToWidth(I18n.format(hint),Math.max(100,width-30));
      for(int i=0;i<Math.min(2,lines.size());i++)drawCenteredString(fontRendererObj,lines.get(i),width/2,height/2+19+i*10,0xAAAAAA);
      super.drawScreen(x,y,partial);
    }
  }
}
