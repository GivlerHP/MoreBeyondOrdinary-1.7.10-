package ru.givler.mbo.potion;

/** Passive combat multiplier; AttackDelay consumes it directly without per-tick mutations. */
public final class CombatStatPotion extends PotionBasic {
    public CombatStatPotion(int id,String name,int color) { this(id,name,color,false); }
    public CombatStatPotion(int id,String name,int color,boolean bad) { super(id,bad,color);setPotionName(name); }
}
