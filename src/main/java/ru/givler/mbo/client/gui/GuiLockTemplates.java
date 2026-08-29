package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketApplyLockTemplate;

public class GuiLockTemplates extends GuiScreen {
    private final int x,y,z; private final NBTTagCompound current;
    private NBTTagList templates; private GuiTextField name; private int selected=-1,scroll;
    public GuiLockTemplates(int x,int y,int z,NBTTagCompound current){this.x=x;this.y=y;this.z=z;this.current=current;}

    @Override public void initGui(){Keyboard.enableRepeatEvents(true);templates=LockTemplateStore.load();int left=width/2-150,top=height/2-105;
        name=new GuiTextField(fontRendererObj,left,top+184,190,18);name.setMaxStringLength(48);name.setText(I18n.format("mbo.lock.template.defaultName",templates.tagCount()+1));
        buttonList.add(new GuiButton(1,left+194,top+183,106,20,I18n.format("mbo.lock.template.save")));
        buttonList.add(new GuiButton(2,left,top+207,96,20,I18n.format("mbo.lock.template.apply")));
        buttonList.add(new GuiButton(3,left+102,top+207,96,20,I18n.format("mbo.lock.template.delete")));
        buttonList.add(new GuiButton(4,left+204,top+207,96,20,I18n.format("gui.back")));updateButtons();}

    private void updateButtons(){for(Object o:buttonList){GuiButton b=(GuiButton)o;if(b.id==1)b.enabled=current!=null&&!name.getText().trim().isEmpty();if(b.id==2||b.id==3)b.enabled=selected>=0&&selected<templates.tagCount();}}
    @Override protected void actionPerformed(GuiButton b){
        if(b.id==1&&current!=null){NBTTagCompound saved=(NBTTagCompound)current.copy();saved.setString("Name",name.getText().trim());templates.appendTag(saved);LockTemplateStore.save(templates);selected=templates.tagCount()-1;scroll=Math.max(0,selected-4);}
        else if(b.id==2&&selected>=0){PacketManager.INSTANCE.sendToServer(new PacketApplyLockTemplate(x,y,z,(NBTTagCompound)templates.getCompoundTagAt(selected).copy()));mc.displayGuiScreen(null);}
        else if(b.id==3&&selected>=0){templates.removeTag(selected);LockTemplateStore.save(templates);selected=-1;}
        else if(b.id==4)mc.displayGuiScreen(null);updateButtons();
    }

    @Override protected void mouseClicked(int mx,int my,int button){super.mouseClicked(mx,my,button);name.mouseClicked(mx,my,button);int left=width/2-150,top=height/2-105;
        if(mx>=left&&mx<left+130&&my>=top+20&&my<top+170){int row=(my-(top+20))/30,index=scroll+row;if(index<templates.tagCount()){selected=index;name.setText(templates.getCompoundTagAt(index).getString("Name"));updateButtons();}}}
    @Override protected void keyTyped(char ch,int key){if(name.textboxKeyTyped(ch,key)){updateButtons();return;}super.keyTyped(ch,key);}
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel!=0){scroll+=wheel<0?1:-1;scroll=Math.max(0,Math.min(Math.max(0,templates.tagCount()-5),scroll));}}
    @Override public void updateScreen(){name.updateCursorCounter();}

    @Override public void drawScreen(int mx,int my,float partial){drawDefaultBackground();int left=width/2-150,top=height/2-105;
        drawRect(left-5,top-5,left+305,top+232,0xdd181818);drawCenteredString(fontRendererObj,I18n.format("mbo.lock.template.title"),width/2,top+3,0xffffff);
        for(int row=0;row<5;row++){int index=scroll+row,ry=top+20+row*30;if(index>=templates.tagCount())break;NBTTagCompound tag=templates.getCompoundTagAt(index);drawRect(left,ry,left+130,ry+26,index==selected?0xff806020:0xff303030);fontRendererObj.drawString(tag.getString("Name"),left+5,ry+9,0xffffff);}
        if(selected>=0&&selected<templates.tagCount())drawPreview(templates.getCompoundTagAt(selected),left+140,top+24);
        fontRendererObj.drawString(I18n.format("mbo.lock.template.name"),left,top+173,0xdddddd);super.drawScreen(mx,my,partial);name.drawTextBox();}

    private void drawPreview(NBTTagCompound tag,int px,int py){NBTTagCompound settings=tag.getCompoundTag("Settings");
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.difficulty.value",I18n.format("mbo.lock.difficulty."+ru.givler.mbo.lockable.LockDifficulty.byOrdinal(tag.getInteger("Difficulty")).name().toLowerCase())),px,py,0xffffff);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.slots.short",settings.getInteger("FillSlots")),px,py+12,0xcccccc);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.cooldown.short",settings.getInteger("Cooldown")),px,py+24,0xcccccc);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.refill.value",I18n.format("mbo.lock.refill."+ru.givler.mbo.lockable.RefillMode.byOrdinal(settings.getInteger("RefillMode")).name().toLowerCase())),px,py+36,0xcccccc);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.selection.value",I18n.format(settings.getBoolean("Weighted")?"mbo.lock.selection.weighted":"mbo.lock.selection.equal")),px,py+48,0xcccccc);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.interval.short",settings.getInteger("GradualInterval"))+" / "+I18n.format("mbo.lock.gui.count.short",settings.getInteger("GradualCount")),px,py+60,0xcccccc);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.radius.short",settings.getInteger("PlayerRadius")),px,py+72,0xcccccc);
        String group=settings.getString("GroupId");if(!group.isEmpty())fontRendererObj.drawString(I18n.format("mbo.lock.gui.group.preview",group),px,py+84,0xcccccc);
        NBTTagList items=tag.getTagList("Items",10);RenderHelper.enableGUIStandardItemLighting();
        for(int i=0;i<items.tagCount();i++){NBTTagCompound item=items.getCompoundTagAt(i);int slot=item.getByte("Slot")&255;ItemStack stack=ItemStack.loadItemStackFromNBT(item);if(stack!=null)itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),stack,px+(slot%7)*18,py+106+(slot/7)*18);}
        RenderHelper.disableStandardItemLighting();}
    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);super.onGuiClosed();}
    @Override public boolean doesGuiPauseGame(){return false;}
}
