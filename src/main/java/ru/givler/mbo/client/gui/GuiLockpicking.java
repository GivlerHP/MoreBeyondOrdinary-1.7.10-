package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketLockpickPin;
import ru.givler.mbo.network.packet.PacketLockpickSuccess;

public class GuiLockpicking extends GuiScreen {
    private static final ResourceLocation TEXTURE=new ResourceLocation("mbo:textures/gui/lockpicking.png");
    private final int x,y,z,pinCount,imageWidth;
    private final boolean[] moved,pinCorrect,pinMoving;
    private final int[] pinY,pinMotion;
    private int selected,order,lockpickX,lockpickMotion,rotation,rotationMotion,closeCountdown=-1;
    private boolean rotating,waiting;
    public GuiLockpicking(int x,int y,int z,int count){this.x=x;this.y=y;this.z=z;pinCount=count;imageWidth=24+count*24;moved=new boolean[count];pinCorrect=new boolean[count];pinMoving=new boolean[count];pinY=new int[count];pinMotion=new int[count];}
    @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();super.drawScreen(mx,my,partial);GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(TEXTURE);int left=(width-imageWidth)/2,top=(height-64)/2;for(int i=0;i<pinCount+1;i++)drawTexturedModalRect(left+i*24,top-48,24,0,24,120);for(int i=0;i<pinCount;i++)drawTexturedModalRect(left+i*24+12,top,0,0,24,64);drawTexturedModalRect(left+pinCount*24+12,top,0,0,12,64);GL11.glPushMatrix();GL11.glTranslatef(left-78+lockpickX,top+24,0);GL11.glRotatef(rotation,0,0,1);drawTexturedModalRect(-78,0,60,0,192,24);GL11.glPopMatrix();for(int i=0;i<pinCount;i++)drawTexturedModalRect(left+i*24+18,top-48,48,48,12,24);for(int i=0;i<pinCount;i++)drawTexturedModalRect(left+i*24+18,top+pinY[i]-24,48,0,12,48);drawTexturedModalRect(left,top,0,0,12,64);drawString(fontRendererObj,I18n.format("mbo.lock.gui.title"),5,5,0xffffff);if(allMoved()){drawCenteredString(fontRendererObj,I18n.format("mbo.lock.gui.great"),width/2,top-75,0xffffff);drawCenteredString(fontRendererObj,I18n.format("mbo.lock.gui.enter"),width/2,top-65,0xffffff);}else{drawCenteredString(fontRendererObj,I18n.format("mbo.lock.gui.orderHelp"),width/2,top+80,0xffffff);drawCenteredString(fontRendererObj,I18n.format("mbo.lock.gui.breakHelp"),width/2,top+90,0xffffff);drawCenteredString(fontRendererObj,I18n.format("mbo.lock.gui.help"),width/2,top+100,0xffffff);}}
    @Override public void updateScreen(){super.updateScreen();for(int i=0;i<pinCount;i++)if(pinCorrect[i]){if(pinMotion[i]>0){pinY[i]-=4;pinMotion[i]--;}}else{if(pinMotion[i]==0)pinMoving[i]=false;if(pinMotion[i]>3){pinY[i]-=6;pinMotion[i]--;}else if(pinMotion[i]>0){pinY[i]+=6;pinMotion[i]--;}}if(lockpickMotion<0){lockpickX-=4;lockpickMotion++;}else if(lockpickMotion>0){lockpickX+=4;lockpickMotion--;}if(rotationMotion==0)rotating=false;if(rotationMotion>5){rotation--;rotationMotion--;}else if(rotationMotion>0){rotation++;rotationMotion--;}if(closeCountdown>=0&&--closeCountdown<=0)mc.displayGuiScreen(null);}
    @Override protected void keyTyped(char ch,int key){if(key==Keyboard.KEY_LEFT||key==mc.gameSettings.keyBindLeft.getKeyCode()){if(selected>0){selected--;lockpickMotion-=6;}}else if(key==Keyboard.KEY_RIGHT||key==mc.gameSettings.keyBindRight.getKeyCode()){if(selected<pinCount-1){selected++;lockpickMotion+=6;}}else if(key==Keyboard.KEY_UP||key==mc.gameSettings.keyBindForward.getKeyCode()){if(!moved[selected]&&!allMoved()&&!waiting){if(!rotating){rotationMotion=10;rotating=true;}waiting=true;PacketManager.INSTANCE.sendToServer(new PacketLockpickPin(x,y,z,selected,order));}}else if(key==Keyboard.KEY_RETURN&&allMoved()){PacketManager.INSTANCE.sendToServer(new PacketLockpickSuccess(x,y,z));mc.displayGuiScreen(null);}else super.keyTyped(ch,key);}
    public void handleResult(boolean correct,int pin,boolean reset,boolean complete,boolean close){waiting=false;if(reset)resetGui();if(close){mc.displayGuiScreen(null);return;}if(correct){moved[pin]=true;order++;pinMotion[pin]=6;pinCorrect[pin]=true;}else if(pin>=0&&pin<pinMoving.length&&!pinMoving[pin]){pinMotion[pin]=6;pinCorrect[pin]=false;pinMoving[pin]=true;}}
    private void resetGui(){order=0;selected=0;lockpickMotion=0;lockpickX=0;rotation=0;rotationMotion=0;for(int i=0;i<moved.length;i++){moved[i]=false;pinCorrect[i]=false;pinMotion[i]=0;pinY[i]=0;pinMoving[i]=false;}}
    private boolean allMoved(){for(boolean value:moved)if(!value)return false;return true;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
