package ru.givler.mbo.item;

import java.lang.reflect.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.registry.EnchantmentRegistry;
import ru.givler.mbo.registry.PotionRegistry;
import ru.givler.mbo.integration.attackdelay.AttackDelayCombatIntegration;

/** Exercise the real AttackDelay API with MBO enchantments/effects, not tooltip-only calculations. */
public final class CombatModifiersSmoke {
    public static final class Fighter extends net.minecraft.entity.player.EntityPlayerMP {
        PotionEffect haste,reach,slowness,shortReach;
        private Fighter(){super(null,null,null,null);}
        @Override public PotionEffect getActivePotionEffect(Potion potion) {
            return potion==PotionRegistry.AttackHaste?haste:potion==PotionRegistry.LongReach?reach
                    :potion==PotionRegistry.AttackSlowness?slowness:potion==PotionRegistry.ShortReach?shortReach:null;
        }
    }
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
        EnchantmentRegistry.register();
        int potionId=-1;
        for(int i=24;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]==null){potionId=i;break;}
        check(potionId>=0,"No test potion slot");
        PotionRegistry.AttackHaste=new ru.givler.mbo.potion.CombatStatPotion(potionId,"potion.attack_haste",0);
        for(int i=potionId+1;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]==null){potionId=i;break;}
        PotionRegistry.LongReach=new ru.givler.mbo.potion.CombatStatPotion(potionId,"potion.long_reach",0);
        PotionRegistry.AttackSlowness=new ru.givler.mbo.potion.CombatStatPotion(freePotion(),"potion.attack_slowness",0,true);
        PotionRegistry.ShortReach=new ru.givler.mbo.potion.CombatStatPotion(freePotion(),"potion.short_reach",0,true);
        ItemStack weapon=new ItemStack(new ItemSword(Item.ToolMaterial.IRON));
        WeaponSettings.write(weapon,new double[]{8,300,125,200,0,100,0});
        weapon.addEnchantment(EnchantmentRegistry.ATTACK_SPEED,3);
        weapon.addEnchantment(EnchantmentRegistry.EXTENDED_REACH,2);
        String nbt=weapon.getTagCompound().toString();
        Fighter user=(Fighter)unsafe.allocateInstance(Fighter.class);
        user.haste=new PotionEffect(PotionRegistry.AttackHaste.id,200,1);
        user.reach=new PotionEffect(PotionRegistry.LongReach.id,200,0);
        check(Math.abs(AttackDelayCombatIntegration.speed(user,weapon)-1.56F)<.001,"Speed stacking mismatch");
        check(Math.abs(AttackDelayCombatIntegration.reach(user,weapon)-1.32F)<.001,"Reach stacking mismatch");
        AttackDelayCombatIntegration.registerApi();
        AttackDelayCombatIntegration.registerApi();
        Class<?> api=Class.forName("ru.givler.attackdelay.common.WeaponStats");
        Method time=api.getMethod("getAttackTimePlayer",ItemStack.class,EntityLivingBase.class);
        Method reach=api.getMethod("getMeleeReachFactor",ItemStack.class,EntityLivingBase.class);
        check(((Number)time.invoke(null,weapon,user)).intValue()==10,"Actual cooldown ignored MBO effects or registration duplicated");
        check(Math.abs(((Number)reach.invoke(null,weapon,user)).floatValue()-2.64F)<.001,"Actual reach ignored MBO effects");
        check(((Number)time.invoke(null,weapon,null)).intValue()==12,"Item-only enchantment bonus missing");
        check(Math.abs(WeaponSettings.attackDelay(weapon,true)-125)<.001,"Editor baseline was polluted by enchantment/effect");
        user.haste=null;user.reach=null;
        check(((Number)time.invoke(null,weapon,user)).intValue()==12,"Expired speed effect remains active");
        check(Math.abs(((Number)reach.invoke(null,weapon,user)).floatValue()-2.4F)<.001,"Expired reach effect remains active");
        ItemStack plain=new ItemStack(new ItemSword(Item.ToolMaterial.IRON));
        check(((Number)time.invoke(null,plain,user)).intValue()==20,"Enchantments leaked across weapons");
        check(((Number)reach.invoke(null,plain,user)).floatValue()==1F,"Reach leaked across weapons");
        check(nbt.equals(weapon.getTagCompound().toString()),"Combat bonus evaluation changed weapon NBT");
        check(EnchantmentRegistry.ATTACK_SPEED.getMaxLevel()==3&&EnchantmentRegistry.EXTENDED_REACH.getMaxLevel()==3,"Wrong enchantment levels");
        check(EnchantmentRegistry.ATTACK_SPEED.canApplyAtEnchantingTable(weapon)
                && !EnchantmentRegistry.ATTACK_SPEED.canApplyAtEnchantingTable(new ItemStack(new Item())),"Enchanting eligibility mismatch");
        check(!EnchantmentRegistry.isTemporary(EnchantmentRegistry.ATTACK_SPEED.effectId),"Permanent enchantment marked temporary");
        boolean inBooks=false;for(Enchantment enchantment:Enchantment.enchantmentsBookList)if(enchantment==EnchantmentRegistry.ATTACK_SPEED)inBooks=true;
        check(inBooks,"Permanent enchantment missing from book list");
        ItemStack cursed=new ItemStack(new ru.givler.mbo.item.magic.ItemSpectralWeapon(Item.ToolMaterial.IRON,300));
        cursed.addEnchantment(EnchantmentRegistry.ATTACK_SLOWNESS,2);
        cursed.addEnchantment(EnchantmentRegistry.SHORT_REACH,3);
        check(((Number)time.invoke(null,cursed,user)).intValue()==25,"Negative speed enchantment ignored");
        check(Math.abs(((Number)reach.invoke(null,cursed,user)).floatValue()-.7F)<.001,"Negative reach enchantment ignored");
        user.slowness=new PotionEffect(PotionRegistry.AttackSlowness.id,200,1);
        user.shortReach=new PotionEffect(PotionRegistry.ShortReach.id,200,0);
        check(((Number)time.invoke(null,cursed,user)).intValue()==31,"Negative enchantment+effect speed stacking mismatch");
        check(Math.abs(((Number)reach.invoke(null,cursed,user)).floatValue()-.63F)<.001,"Negative reach stacking mismatch");
        user.haste=new PotionEffect(PotionRegistry.AttackHaste.id,200,1);
        user.reach=new PotionEffect(PotionRegistry.LongReach.id,200,0);
        check(Math.abs(AttackDelayCombatIntegration.speed(user,weapon)-1.248F)<.001,"Buff/debuff speed combination mismatch");
        check(Math.abs(AttackDelayCombatIntegration.reach(user,weapon)-1.188F)<.001,"Buff/debuff reach combination mismatch");
        user.haste=null;user.reach=null;
        user.slowness=new PotionEffect(PotionRegistry.AttackSlowness.id,200,255);
        user.shortReach=new PotionEffect(PotionRegistry.ShortReach.id,200,255);
        check(((Number)time.invoke(null,cursed,user)).intValue()>0
                && ((Number)reach.invoke(null,cursed,user)).floatValue()>0,"High-level penalties became invalid/negative");
        user.slowness=null;user.shortReach=null;
        check(((Number)time.invoke(null,cursed,user)).intValue()==25,"Removed negative effect remained active");
        check(PotionRegistry.AttackSlowness.isBadEffect()&&PotionRegistry.ShortReach.isBadEffect(),"Debuffs not marked harmful");
        check(!EnchantmentRegistry.ATTACK_SPEED.canApplyTogether(EnchantmentRegistry.ATTACK_SLOWNESS)
                && !EnchantmentRegistry.ATTACK_SLOWNESS.canApplyTogether(EnchantmentRegistry.ATTACK_SPEED)
                && !EnchantmentRegistry.SHORT_REACH.canApplyTogether(EnchantmentRegistry.EXTENDED_REACH),"Opposite enchantments compatible");
        check(!EnchantmentRegistry.ATTACK_SLOWNESS.canApplyAtEnchantingTable(cursed)
                && EnchantmentRegistry.ATTACK_SLOWNESS.canApply(cursed),"Negative enchantment application rules");
        java.util.List<String> rows=new java.util.ArrayList<String>(java.util.Arrays.asList("Sword",
                EnchantmentRegistry.ATTACK_SLOWNESS.getTranslatedName(2),EnchantmentRegistry.SHORT_REACH.getTranslatedName(3)));
        ru.givler.mbo.client.gui.EquipmentTooltipDetails.decorate(cursed,rows,true);
        check(rows.toString().contains("<mbo:enchants>"),"Red negative enchantment names lost their tooltip section");
        System.out.println("Combat modifiers passed: real AttackDelay/MBO integration, enchantment+effect stacking, item overrides, expiry, weapon switch, book/table support and immutable NBT");
    }
    private static int freePotion() {
        for(int i=24;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]==null)return i;
        throw new AssertionError("No free potion ID for test");
    }
}
