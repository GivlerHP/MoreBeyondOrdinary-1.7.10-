package ru.givler.mbo.handler;

public enum MboGui {
    INFUSION_WORKBENCH(0),
    LOOT_CONTAINER_CONFIG(1),
    LOOM(2),
    STONECUTTER(3),
    BARREL(4),
    LOCKPICKING(5),
    LOCKABLE_CHEST(6),
    LOCK_CONFIG(7);

    public final int id;

    MboGui(int id) { this.id = id; }

    public static MboGui byId(int id) {
        for (MboGui gui : values()) if (gui.id == id) return gui;
        return null;
    }
}
