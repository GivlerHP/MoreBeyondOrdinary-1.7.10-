package ru.givler.mbo.gui.lootcontainer;

public class LootActionDropdownOption implements Comparable<LootActionDropdownOption> {
    public final String label;
    public final String value;

    public LootActionDropdownOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public int compareTo(LootActionDropdownOption other) {
        return this.label.compareTo(other.label);
    }
}
