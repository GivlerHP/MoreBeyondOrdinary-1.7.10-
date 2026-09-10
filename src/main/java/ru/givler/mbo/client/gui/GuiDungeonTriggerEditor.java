package ru.givler.mbo.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.ScaledResolution;
import ru.givler.mbo.client.gui.lootcontainer.ActionEditorHost;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketDungeonAreaDelete;
import ru.givler.mbo.network.packet.PacketDungeonTriggerSettings;

public final class GuiDungeonTriggerEditor extends GuiScreen implements ActionEditorHost {
    private final String id;private int repeatMode,cooldown=5;private boolean players=true,entities,projectiles,explosions,multi;private final List<LootContainerActionEditor> editors=new ArrayList<LootContainerActionEditor>();private List<LootContainerAction> initial=new ArrayList<LootContainerAction>();private GuiTextField cooldownField;private int scroll;
    public GuiDungeonTriggerEditor(){this(new NBTTagCompound());}
    public GuiDungeonTriggerEditor(NBTTagCompound tag){id=tag.getString("Id");repeatMode=tag.getInteger("RepeatMode");cooldown=Math.max(1,tag.getInteger("CooldownSeconds"));players=!tag.hasKey("TriggerPlayers")||tag.getBoolean("TriggerPlayers");entities=tag.getBoolean("TriggerEntities");projectiles=tag.getBoolean("TriggerProjectiles");explosions=tag.getBoolean("TriggerExplosions");multi=tag.getBoolean("AllowMultiaction");try{initial=LootContainerAction.fromJsonList(tag.getString("Actions"));}catch(Exception ignored){}}
    @Override public void initGui(){Keyboard.enableRepeatEvents(true);buttonList.clear();editors.clear();int left=width/2-215;cooldownField=new GuiTextField(fontRendererObj,left+250,45,80,18);cooldownField.setText(String.valueOf(cooldown));buttonList.add(new GuiButton(100,left,20,210,20,modeLabel()));buttonList.add(new GuiButton(101,left,45,115,20,toggle("mbo.trigger.players",players)));buttonList.add(new GuiButton(102,left+120,45,115,20,toggle("mbo.trigger.entities",entities)));buttonList.add(new GuiButton(103,left,70,115,20,toggle("mbo.trigger.projectiles",projectiles)));buttonList.add(new GuiButton(104,left+120,70,115,20,toggle("mbo.trigger.explosions",explosions)));buttonList.add(new GuiButton(105,left+240,70,180,20,toggle("mbo.loot.gui.multiaction",multi)));int y=105-scroll;for(int i=0;i<LootContainerData.MAX_ACTIONS;i++){LootContainerAction a=i<initial.size()?initial.get(i):null;LootContainerActionEditor e=new LootContainerActionEditor(i,a,fontRendererObj,buttonList,this);e.init(left,y,430);editors.add(e);y+=e.getHeight();}buttonList.add(new GuiButton(1,left,height-26,100,20,I18n.format("mbo.platform.save")));buttonList.add(new GuiButton(2,left+105,height-26,100,20,I18n.format("gui.cancel")));if(!id.isEmpty())buttonList.add(new GuiButton(3,left+310,height-26,110,20,I18n.format("mbo.dungeon.delete")));updateVisibility();for(Object value:buttonList){GuiButton b=(GuiButton)value;if(b.id>=5000)b.visible=b.yPosition>=100&&b.yPosition+b.height<=height-30;}}
    private void updateVisibility(){cooldownField.setVisible(repeatMode==2);}
    private String modeLabel(){return I18n.format("mbo.trigger.repeat")+": "+I18n.format("mbo.trigger.repeat."+repeatMode);}
    private String toggle(String key,boolean value){return (value?"[x] ":"[ ] ")+I18n.format(key);}
    @Override protected void actionPerformed(GuiButton b){if(b.id==100){repeatMode=(repeatMode+1)%3;b.displayString=modeLabel();updateVisibility();return;}if(b.id>=101&&b.id<=105){if(b.id==101)players=!players;if(b.id==102)entities=!entities;if(b.id==103)projectiles=!projectiles;if(b.id==104)explosions=!explosions;if(b.id==105)multi=!multi;String key=b.id==101?"mbo.trigger.players":b.id==102?"mbo.trigger.entities":b.id==103?"mbo.trigger.projectiles":b.id==104?"mbo.trigger.explosions":"mbo.loot.gui.multiaction";boolean value=b.id==101?players:b.id==102?entities:b.id==103?projectiles:b.id==104?explosions:multi;b.displayString=toggle(key,value);return;}if(b.id>=5000&&b.id<5000+editors.size()){editors.get(b.id-5000).cycleType();relayout();return;}for(LootContainerActionEditor e:editors)if(e.actionPerformed(b))return;if(b.id==1){send();mc.displayGuiScreen(null);}if(b.id==2)mc.displayGuiScreen(null);if(b.id==3){PacketManager.INSTANCE.sendToServer(new PacketDungeonAreaDelete(id));mc.displayGuiScreen(null);}}
    private void relayout(){List<LootContainerAction> values=collect();initial=values;initGui();}
    private List<LootContainerAction> collect(){List<LootContainerAction> out=new ArrayList<LootContainerAction>();for(LootContainerActionEditor e:editors){LootContainerAction a=e.toData();if(a!=null)out.add(a);}return out;}
    private void send(){NBTTagCompound tag=new NBTTagCompound();tag.setString("Id",id);tag.setInteger("RepeatMode",repeatMode);tag.setInteger("CooldownSeconds",number(cooldownField.getText(),cooldown));tag.setBoolean("Players",players);tag.setBoolean("Entities",entities);tag.setBoolean("Projectiles",projectiles);tag.setBoolean("Explosions",explosions);tag.setBoolean("Multi",multi);tag.setString("Actions",LootContainerAction.toJsonList(collect()));PacketManager.INSTANCE.sendToServer(new PacketDungeonTriggerSettings(tag));}
    private static int number(String s,int fallback){try{return Math.max(1,Integer.parseInt(s));}catch(Exception ignored){return fallback;}}
    public void onItemPicked(int index,ItemStack stack){if(index>=0&&index<editors.size())editors.get(index).setPickedItem(stack);}
    @Override public void openItemPicker(int index){mc.displayGuiScreen(new GuiLootContainerItemPicker(this,this,index,mc.thePlayer));}
    @Override protected void keyTyped(char c,int k){if(cooldownField.getVisible()&&cooldownField.textboxKeyTyped(c,k))return;for(LootContainerActionEditor e:editors)if(e.keyTyped(c,k))return;super.keyTyped(c,k);}
    @Override protected void mouseClicked(int x,int y,int b){for(LootContainerActionEditor e:editors)if(e.tryClickDropdown(x,y,b))return;super.mouseClicked(x,y,b);if(cooldownField.getVisible())cooldownField.mouseClicked(x,y,b);for(LootContainerActionEditor e:editors)e.mouseClicked(x,y,b);}
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0){cooldown=number(cooldownField.getText(),cooldown);int total=0;for(LootContainerActionEditor e:editors)total+=e.getHeight();int max=Math.max(0,105+total-(height-30));scroll=Math.max(0,Math.min(max,scroll+(wheel<0?30:-30)));relayout();}}
    @Override public void drawScreen(int mx,int my,float p){drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("mbo.trigger.settings"),width/2,6,0xffffff);if(cooldownField.getVisible()){drawString(fontRendererObj,I18n.format("mbo.trigger.cooldown"),width/2+35,32,0xaaaaaa);cooldownField.drawTextBox();}ScaledResolution sr=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);int scale=sr.getScaleFactor();GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(0,(height-(height-30))*scale,width*scale,(height-130)*scale);for(LootContainerActionEditor e:editors)e.draw(mx,my);GL11.glDisable(GL11.GL_SCISSOR_TEST);super.drawScreen(mx,my,p);for(LootContainerActionEditor e:editors)e.drawOverlay(mx,my);}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
