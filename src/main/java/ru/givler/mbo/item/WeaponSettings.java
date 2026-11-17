package ru.givler.mbo.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Per-stack overrides. Never mutates shared Item instances or their material definitions. */
public final class WeaponSettings {
    public static final String KEY = "MBO_WeaponSettings";
    public static final UUID DAMAGE_ID = UUID.fromString("d1a4a4e2-1780-48e7-913c-a7a4655f4c4d");
    private WeaponSettings() {}
    public static boolean supports(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        net.minecraft.item.Item item = stack.getItem();
        if (item instanceof net.minecraft.item.ItemSword || item instanceof net.minecraft.item.ItemTool) return true;
        String name = item.getClass().getName();
        return name.startsWith("minefantasy.mf2.item.weapon.") && !name.endsWith("ItemShieldMF")
                || name.equals("minefantasy.mf2.item.tool.advanced.ItemScythe");
    }
    public static NBTTagCompound data(ItemStack stack) {
        return stack != null && stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag(KEY) : new NBTTagCompound();
    }
    private static boolean configured(ItemStack stack) {
        return stack!=null && stack.hasTagCompound() && stack.getTagCompound().hasKey(KEY,10);
    }
    public static boolean valid(double[] values) {
        if (values == null || values.length != 7) return false;
        for (double value : values) if (Double.isNaN(value) || Double.isInfinite(value)) return false;
        return values[0]>=0 && values[0]<=10000 && values[1]>=1 && values[1]<=10000000
                && values[1]==Math.rint(values[1]) && values[2]>=5 && values[2]<=1000
                && values[3]>=10 && values[3]<=1000 && values[4]>=0 && values[5]>=0 && values[6]>=0
                && values[4]<=100 && values[5]<=100 && values[6]<=100 && values[4]+values[5]+values[6]>0;
    }
    public static void write(ItemStack stack, double[] values) {
        if (!supports(stack) || !valid(values)) throw new IllegalArgumentException("Invalid weapon settings");
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("Damage", values[0]); tag.setInteger("Durability", (int)values[1]);
        tag.setFloat("Speed", (float)(values[2]/100)); tag.setFloat("Reach", (float)(values[3]/100));
        double total=values[4]+values[5]+values[6];
        tag.setFloat("Cutting", (float)(values[4]/total)); tag.setFloat("Piercing", (float)(values[5]/total));
        tag.setFloat("Blunt", (float)(values[6]/total));
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag(KEY, tag);
    }
    public static void reset(ItemStack stack) { if (stack != null && stack.hasTagCompound()) stack.getTagCompound().removeTag(KEY); }
    public static ItemStack original(ItemStack stack) { ItemStack copy=stack.copy(); reset(copy); return copy; }
    public static int maxDamage(int original, ItemStack stack) {
        if(!configured(stack))return original;
        NBTTagCompound tag=data(stack); int value=tag.getInteger("Durability");
        return tag.hasKey("Durability", 3) && value>=1 && value<=10000000 ? value : original;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Multimap attributes(Multimap original, ItemStack stack) {
        if(!configured(stack))return original;
        NBTTagCompound tag=data(stack);
        double damage=tag.getDouble("Damage");
        if (!tag.hasKey("Damage") || !Double.isFinite(damage) || damage<0 || damage>10000) return original;
        Multimap result=HashMultimap.create(original);
        result.removeAll("generic.attackDamage");
        result.put("generic.attackDamage", new AttributeModifier(DAMAGE_ID,"MBO individual weapon damage",damage,0));
        return result;
    }
    public static float[] ratio(float[] original, ItemStack stack) {
        if(!configured(stack))return original;
        NBTTagCompound tag=data(stack);
        if (!tag.hasKey("Cutting") || !tag.hasKey("Piercing") || !tag.hasKey("Blunt")) return original;
        float c=tag.getFloat("Cutting"), p=tag.getFloat("Piercing"), b=tag.getFloat("Blunt");
        float total=c+p+b;
        if (!Float.isFinite(total) || c<0 || p<0 || b<0 || total<=0) return original;
        return new float[]{c/total,p/total,b/total};
    }
    public static double damage(ItemStack stack) {
        double total=0;
        for(Object value:stack.getAttributeModifiers().get("generic.attackDamage")) {
            AttributeModifier modifier=(AttributeModifier)value;
            if(modifier.getOperation()==0) total+=modifier.getAmount();
        }
        return total;
    }
    public static double attackDelay(ItemStack stack, boolean speed) {
        try {
            Class<?> type=Class.forName("ru.givler.attackdelay.common.WeaponStats");
            java.lang.reflect.Method getter;
            try {getter=type.getMethod(speed?"getBaseMeleeSpeed":"getBaseMeleeReachFactor",ItemStack.class);}
            catch(NoSuchMethodException oldVersion) {getter=type.getMethod(speed?"getMeleeSpeed":"getMeleeReachFactor",ItemStack.class);}
            return ((Number)getter.invoke(null,stack)).doubleValue()*100;
        } catch (ReflectiveOperationException absent) { return 100; }
    }
}
