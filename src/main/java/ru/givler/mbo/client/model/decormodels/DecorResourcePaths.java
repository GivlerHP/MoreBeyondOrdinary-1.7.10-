package ru.givler.mbo.client.model.decormodels;

/** Resolves saved model names into the shared geometry and texture folders. */
public final class DecorResourcePaths {
    private DecorResourcePaths() {}
    private static boolean imported(String name) {
        return name != null && name.startsWith("instruments_of_torture/");
    }
    public static String localName(String name) {
        // The original keys model is a different asset and must keep its name.
        if ("instruments_of_torture/keys".equals(name)) return "torture_keys";
        return imported(name) ? name.substring("instruments_of_torture/".length()) : name;
    }
    public static String geometry(String name) {
        return "geo/" + localName(name) + ".geo.json";
    }
    public static String texture(String name, String suffix) {
        return "textures/models/decor/" + localName(name) + suffix + ".png";
    }
}
