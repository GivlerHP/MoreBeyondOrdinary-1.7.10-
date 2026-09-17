package ru.givler.mbo.potion;

public class StaticAura extends MagicStatus {
    public StaticAura(int id, boolean harmful, int colour) {
        super(id, harmful, colour, "static_aura", 3);
    }
}
