package ru.givler.mbo.gui.lootcontainer;

import java.util.List;

public class LootActionDropdownState {
    public final List<LootActionDropdownOption> matches;
    public final int shown;
    public final int scroll;
    public final int maxScroll;
    public final int boxX;
    public final int boxY;
    public final int boxWidth;
    public final int boxHeight;

    public LootActionDropdownState(List<LootActionDropdownOption> matches,
                                   int shown,
                                   int scroll,
                                   int maxScroll,
                                   int boxX,
                                   int boxY,
                                   int boxWidth,
                                   int boxHeight) {
        this.matches = matches;
        this.shown = shown;
        this.scroll = scroll;
        this.maxScroll = maxScroll;
        this.boxX = boxX;
        this.boxY = boxY;
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
    }
}
