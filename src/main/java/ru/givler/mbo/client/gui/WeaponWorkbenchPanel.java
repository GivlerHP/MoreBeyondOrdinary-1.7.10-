package ru.givler.mbo.client.gui;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.item.WeaponSettings;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketWeaponSettings;

/** Numeric draft editor with source/effective comparison; only Save sends changes to the server. */
final class WeaponWorkbenchPanel {
    private static final String[] KEYS={"damage","durability","speed","reach","cutting","piercing","blunt"};
    private final List<GuiTextField> fields=new ArrayList<GuiTextField>();
    private final List<GuiButton> buttons=new ArrayList<GuiButton>();
    private double[] base=new double[7];
    private ItemStack editing;
    private FontRenderer font;
    private int left,top,width,previewX;
    private boolean delay;

    void init(FontRenderer font,int left,int top,int width,int previewX) {
        this.font=font;this.left=left;this.top=top;this.width=width;this.previewX=previewX;
        delay=cpw.mods.fml.common.Loader.isModLoaded("attackdelay");
        fields.clear();buttons.clear();
        for(int i=0;i<7;i++) {
            int y=top+43+i*19;
            GuiTextField field=new GuiTextField(font,left+width-76,y+3,44,10);
            field.setMaxStringLength(10);field.setEnableBackgroundDrawing(false);fields.add(field);
            buttons.add(new GuiButton(90+i*2,left+width-94,y,16,16,"−"));
            buttons.add(new GuiButton(91+i*2,left+width-29,y,16,16,"+"));
        }
        buttons.add(new GuiButton(80,left+12,top+182,(width-28)/2,20,I18n.format("mbo.description.save")));
        buttons.add(new GuiButton(81,left+16+(width-28)/2,top+182,(width-28)/2,20,I18n.format("mbo.weapon.reset")));
    }
    private static double[] read(ItemStack stack) {
        double[] values={WeaponSettings.damage(stack),stack.getMaxDamage(),WeaponSettings.attackDelay(stack,true),WeaponSettings.attackDelay(stack,false),1,1,1};
        try {
            Class<?> calculator=Class.forName("minefantasy.mf2.api.helpers.ArmourCalculator");
            float[] ratio=(float[])calculator.getMethod("getRatioForWeapon",ItemStack.class).invoke(null,stack);
            if(ratio!=null && ratio.length==3) {
                double total=ratio[0]+ratio[1]+ratio[2];
                if(total>0)for(int i=0;i<3;i++)values[4+i]=Math.round(ratio[i]/total*10000)/100D;
            }
        } catch(ReflectiveOperationException absent) {}
        return values;
    }
    void load(ItemStack stack) {
        editing=stack==null?null:stack.copy();
        if(!WeaponSettings.supports(stack)) { for(GuiTextField field:fields)field.setText("");return; }
        base=read(WeaponSettings.original(stack));double[] current=read(stack);
        net.minecraft.nbt.NBTTagCompound tag=WeaponSettings.data(stack);
        if(tag.hasKey("Damage"))current[0]=tag.getDouble("Damage");
        if(tag.hasKey("Durability"))current[1]=tag.getInteger("Durability");
        if(tag.hasKey("Speed"))current[2]=tag.getFloat("Speed")*100;
        if(tag.hasKey("Reach"))current[3]=tag.getFloat("Reach")*100;
        if(tag.hasKey("Cutting")) {current[4]=tag.getFloat("Cutting")*100;current[5]=tag.getFloat("Piercing")*100;current[6]=tag.getFloat("Blunt")*100;}
        for(int i=0;i<7;i++)fields.get(i).setText(number(current[i]));
    }
    double[] values() {
        double[] values=new double[7];
        try { for(int i=0;i<7;i++)values[i]=Double.parseDouble(fields.get(i).getText().replace(',','.')); }
        catch(NumberFormatException invalid) { return null; }
        return values;
    }
    private static String number(double value) {return String.format(Locale.ROOT,"%.2f",value).replaceAll("\\.?0+$","");}
    void draw(int mouseX,int mouseY) {
        boolean supported=WeaponSettings.supports(editing);
        font.drawString(I18n.format("mbo.weapon.title"),left+12,top+28,0xdac99f);
        double[] values=values();
        for(int i=0;i<7;i++) {
            int y=top+43+i*19;
            font.drawString(font.trimStringToWidth(I18n.format("mbo.weapon."+KEYS[i]),width-108),left+12,y+4,!delay&&(i==2||i==3)?0x777777:0xdac99f);
            DescriptionWorkbenchSkin.field(left+width-78,y,48,16,fields.get(i).isFocused());
            fields.get(i).setEnabled(supported && (delay || i!=2&&i!=3));
            fields.get(i).setTextColor(0xffffff); fields.get(i).drawTextBox();
        }
        for(GuiButton button:buttons) {
            button.enabled=supported && (button.id==81 || WeaponSettings.valid(values));
            if(button.id>=90) {
                int index=(button.id-90)/2;button.enabled=supported && (delay || index!=2&&index!=3);
            }
            button.drawButton(Minecraft.getMinecraft(),mouseX,mouseY);
        }
        int x=left+previewX+8,y=top+62;
        if(!supported) {font.drawSplitString(I18n.format("mbo.weapon.insert"),x,y,160,0xdac99f);return;}
        if(values==null) {font.drawString(I18n.format("mbo.weapon.invalid"),x,y,0xff5555);return;}
        values=values.clone();
        if(delay && values[2]>0) {
            int baseTime=20;
            try {baseTime=Class.forName("ru.givler.attackdelay.common.WeaponStats").getField("basePlayerMeleeTime").getInt(null);}
            catch(ReflectiveOperationException absent) {}
            values[2]=baseTime*100D/Math.max(1,Math.round(baseTime/(values[2]/100)));
        }
        double sum=values[4]+values[5]+values[6];
        if(sum>0)for(int i=4;i<7;i++)values[i]=values[i]/sum*100;
        for(int i=0;i<7;i++) {
            String unit=i>=2?"%":"";
            font.drawString(I18n.format("mbo.weapon."+KEYS[i])+": "+number(base[i])+unit+" → "+number(values[i])+unit,x,y,0xdac99f);
            double difference=values[i]-base[i];
            String delta=Math.abs(difference)<.005?"=":(difference>0?"+":"")+number(difference);
            font.drawString(delta,x,y+10,difference>0?0x55ff55:difference<0?0xff5555:0x999999);y+=19;
        }
        if(!delay) font.drawString(I18n.format("mbo.weapon.no_delay"),x,top+49,0xffaa55);
    }
    boolean click(int x,int y,int mouseButton,int window) {
        if(mouseButton!=0)return false;
        for(GuiTextField field:fields)field.mouseClicked(x,y,mouseButton);
        for(GuiButton button:buttons)if(button.mousePressed(Minecraft.getMinecraft(),x,y)) {
            double[] values=values();
            if(button.id>=90) {
                int i=(button.id-90)/2;
                double current=values==null?base[i]:values[i];
                double step=i==0?1:i==1?10:5;
                fields.get(i).setText(number(current+((button.id&1)==1?step:-step)));return true;
            }
            if(editing!=null && (button.id==81 || WeaponSettings.valid(values)))
                PacketManager.INSTANCE.sendToServer(new PacketWeaponSettings(window,editing.copy(),values==null?base:values,button.id==81));
            return true;
        }
        return x>=left&&x<left+width&&y>=top+28&&y<top+202;
    }
    boolean key(char c,int key) {
        for(int i=0;i<fields.size();i++)if(fields.get(i).isFocused()) {
            if(!delay&&(i==2||i==3))return true;
            fields.get(i).textboxKeyTyped(c,key);return true;
        }
        return false;
    }
}
