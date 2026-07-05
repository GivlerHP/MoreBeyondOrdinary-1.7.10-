package ru.givler.mbo.registry;

public final class ModEntityIds {
    private static int nextId = 0;

    private ModEntityIds() {}

    public static synchronized int next() {
        return nextId++;
    }
}