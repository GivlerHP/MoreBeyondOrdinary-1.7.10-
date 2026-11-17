package ru.givler.mbo.integration.attackdelay;

import java.lang.reflect.Proxy;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.registry.EnchantmentRegistry;
import ru.givler.mbo.registry.PotionRegistry;

/** Optional API bridge: AttackDelay owns combat timings; MBO contributes enchantment/effect factors. */
public final class AttackDelayCombatIntegration {
    private AttackDelayCombatIntegration() {}
    public static void register() {
        if (!cpw.mods.fml.common.Loader.isModLoaded("attackdelay")) return;
        registerApi();
    }
    public static void registerApi() {
        try {
            Class<?> api=Class.forName("ru.givler.attackdelay.api.CombatStatModifier");
            Object provider=Proxy.newProxyInstance(api.getClassLoader(),new Class<?>[]{api},(proxy,method,args)-> {
                if(method.getName().equals("getSpeedMultiplier")) return speed((EntityLivingBase)args[0],(ItemStack)args[1]);
                if(method.getName().equals("getReachMultiplier")) return reach((EntityLivingBase)args[0],(ItemStack)args[1]);
                if(method.getName().equals("toString")) return "MBO combat enchantments and effects";
                if(method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                if(method.getName().equals("equals")) return proxy==args[0];
                throw new UnsupportedOperationException(method.getName());
            });
            Class.forName("ru.givler.attackdelay.common.WeaponStats").getMethod("registerModifier",String.class,api)
                    .invoke(null,"mbo:combat_bonuses",provider);
            org.apache.logging.log4j.LogManager.getLogger("MBO.AttackDelay").info("Registered MBO combat enchantments/effects in AttackDelay modifier API");
        } catch (ReflectiveOperationException missing) {
            org.apache.logging.log4j.LogManager.getLogger("MBO.AttackDelay").warn(
                    "Combat enchantments/effects require AttackDelay 1.13 or newer",missing);
        }
    }
    public static float speed(EntityLivingBase user,ItemStack stack) {
        return (1F+.1F*level(EnchantmentRegistry.ATTACK_SPEED,stack))
                *(1F+.1F*effectLevel(user,PotionRegistry.AttackHaste))
                *reduction(.1F,level(EnchantmentRegistry.ATTACK_SLOWNESS,stack))
                *reduction(.1F,effectLevel(user,PotionRegistry.AttackSlowness));
    }
    public static float reach(EntityLivingBase user,ItemStack stack) {
        return (1F+.1F*level(EnchantmentRegistry.EXTENDED_REACH,stack))
                *(1F+.1F*effectLevel(user,PotionRegistry.LongReach))
                *reduction(.1F,level(EnchantmentRegistry.SHORT_REACH,stack))
                *reduction(.1F,effectLevel(user,PotionRegistry.ShortReach));
    }
    private static float reduction(float perLevel,int level) { return Math.max(.1F,1F-perLevel*level); }
    private static int level(Enchantment enchantment,ItemStack stack) {
        return enchantment==null || stack==null ? 0 : Math.max(0,Math.min(3,EnchantmentHelper.getEnchantmentLevel(enchantment.effectId,stack)));
    }
    private static int effectLevel(EntityLivingBase user,Potion potion) {
        if(user==null || potion==null) return 0;
        PotionEffect effect=user.getActivePotionEffect(potion);
        return effect==null?0:Math.max(0,Math.min(256,effect.getAmplifier()+1));
    }
}
