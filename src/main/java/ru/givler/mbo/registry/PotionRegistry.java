package ru.givler.mbo.registry;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.potion.Potion;
import ru.givler.mbo.potion.*;

public class PotionRegistry {

    /** Ru-справка по эффектам:
     * MeleeDamage - 20% увеличивает урон ближнего боя за уровень
     * Vampirism - отхиливает на 3% от дамага + 1.5% за уровень
     * Potion Dodge - 10% шанс увернуть от атаки за уровень
     * Hex - 15% при атаке наложить негативный эффект из списка за уровень
     * Phoenix - спасает от сметрельного удара и восстанавливает 2 хп за уровень
     * SixthSense - позволяет видеть ауру всех живых существ в радиусе 20 блоков + 10 за уровень
     * Magnetism - увеличивает радиус поднятия предмета до 3 блоков + 1.5 за уровень
     * BashStun - запрещает двигаться, наносить урон, использовать предметы (оглушение)
     * ApplyStun - накладывает оглушение на 0.5 секунды при атаке с шансом 15% за уровень
     * Vulnerability - увеличивает входящий урон на 10% за уровень
     * DodgeHit - отменяет уклонение
     * Disarm - запрещает атаковать
     * Thorns - возвращает 10% урона за уровень
     * Curse - уменьшает макс. здоровье на 50%
     * Luck - увеличивает шанс дропа с руд на 20% за уровень
     * Stamina - увеличивает восстановление выносливости (MineFantasy Integration)
     */

    public static Potion MeleeDamage;
    public static Potion Vampirism;
    public static Potion Dodge;
    public static Potion Hex;
    public static Potion Phoenix;
    public static Potion SixthSense;
    public static Potion Magnetism;
    public static Potion BashStun;
    public static Potion ApplyStun;
    public static Potion Vulnerability;
    public static Potion DodgeHit;
    public static Potion Disarm;
    public static Potion Thorns;
    public static Potion Curse;
    public static Potion Luck;
    public static Potion Stamina;
    public static Potion Looting;
    public static Potion Frost;
    public static Potion Transience;
    public static Potion Fireskin;
    public static Potion IceShroud;
    public static Potion StaticAura;
    public static Potion Decay;
    public static Potion ArcaneJammer;
    public static Potion MindTrick;
    public static Potion MindControl;
    public static Potion FontOfMana;
    public static Potion Fear;


    public static void preLoad(FMLPreInitializationEvent event) {

        MeleeDamage = new MeleeDamage(nextFreeId(), false, 0xFF0000);
        Vampirism = new Vampirism(nextFreeId(), false, 0x800000);
        Dodge = new Dodge(nextFreeId(), false, 0x9ACD32);
        Hex = new Hex(nextFreeId(), false, 0x4B0082);
        Phoenix = new Phoenix(nextFreeId(), false, 0xD2691E);
        SixthSense = new SixthSense(nextFreeId(), false, 0x6B8E23);
        Magnetism = new Magnetism(nextFreeId(), false, 0x8A2BE2);
        BashStun = new BashStun(nextFreeId(), true, 0x8B4513);
        ApplyStun = new ApplyStun(nextFreeId(), false, 0xA52A2A);
        Vulnerability = new Vulnerability(nextFreeId(), true, 0x708090);
        DodgeHit = new DodgeHit(nextFreeId(), true, 0x808000);
        Disarm = new Disarm(nextFreeId(), true, 0xF8F8FF);
        Thorns = new Thorns(nextFreeId(), false, 0x00BFFF);
        Curse = new Curse(nextFreeId(), true, 0x5B1E31);
        Luck = new Luck(nextFreeId(), false, 0x3CB371);
        Stamina = new Stamina(nextFreeId(), false, 0x44FF88);
        Looting = new Looting(nextFreeId(), false, 0x6B4F2A);

        Frost = new Frost(nextFreeId(), true, 0x38ddec);
        Transience = new Transience(nextFreeId(), false, 0xffe89b);
        Fireskin = new Fireskin(nextFreeId(), false, 0xff2f02);
        IceShroud = new IceShroud(nextFreeId(), false, 0x52f1ff);
        StaticAura = new StaticAura(nextFreeId(), false, 0x0070ff);
        Decay = new Decay(nextFreeId(), true, 0x3c006c);
        ArcaneJammer = new ArcaneJammer(nextFreeId(), true, 0xcf4aa2);
        MindTrick = new MindTrick(nextFreeId(), true, 0x601683);
        MindControl = new MindControl(nextFreeId(), true, 0x320b44);
        FontOfMana = new FontOfMana(nextFreeId(), false, 0xffe5bb);
        Fear = new Fear(nextFreeId(), true, 0xbd0100);
    }

    /** Finds a free expanded potion slot; spell effects never claim fixed numeric IDs. */
    private static int nextFreeId() {
        for (int id = 24; id < Potion.potionTypes.length; id++) {
            if (Potion.potionTypes[id] == null) return id;
        }
        throw new IllegalStateException("No free potion IDs remain for MBO");
    }

}

