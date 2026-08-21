package ru.givler.mbo.core;

public final class RailHooks {
    private static int renderType = 9;

    private RailHooks() { }

    public static int getRenderType() { return renderType; }
    public static void setRenderType(int value) { renderType = value; }
}
