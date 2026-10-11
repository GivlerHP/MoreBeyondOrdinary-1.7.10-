package ru.givler.mbo.client.model.decormodels;

/** Keeps saved resource names stable while placing the new assets in decor. */
public final class DecorResourcePaths {
    private DecorResourcePaths() {}
    private static boolean imported(String name) {
        return name != null && name.startsWith("instruments_of_torture/");
    }
    public static String localName(String name) {
        return imported(name) ? name.substring("instruments_of_torture/".length()) : name;
    }
    public static String geometry(String name) {
        return (imported(name) ? "geo/decor/" : "geo/") + localName(name) + ".geo.json";
    }
    public static String texture(String name, String suffix) {
        // Block icons use the blocks atlas, whose resource root is textures/blocks.
        return (imported(name) ? "textures/blocks/models/decor/" : "textures/models/decor/")
            + localName(name) + suffix + ".png";
    }
}
