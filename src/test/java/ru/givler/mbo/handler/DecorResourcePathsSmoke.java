package ru.givler.mbo.handler;

import java.nio.file.Files;
import java.nio.file.Paths;
import ru.givler.mbo.client.model.decormodels.DecorResourcePaths;

public final class DecorResourcePathsSmoke {
    public static void main(String[] args) {
        String root = "src/main/resources/assets/mbo/";
        String name = "instruments_of_torture/hook";
        check(Files.isRegularFile(Paths.get(root + DecorResourcePaths.geometry(name))), "geometry moved");
        String texture = DecorResourcePaths.texture(name, "");
        check(texture.equals("textures/models/decor/hook.png"), "block atlas resource");
        check(DecorResourcePaths.geometry(name).equals("geo/hook.geo.json"), "flat model folder");
        check(Files.isRegularFile(Paths.get(root + texture)), "particle and render PNG exists");
        check(DecorResourcePaths.geometry("altar").equals("geo/altar.geo.json"), "old model location");
        check(DecorResourcePaths.texture("altar", "").equals("textures/models/decor/altar.png"), "old texture location");
        check(DecorResourcePaths.geometry("instruments_of_torture/keys").equals("geo/torture_keys.geo.json"), "imported keys keep a distinct asset");
        check(DecorResourcePaths.geometry("keys").equals("geo/keys.geo.json"), "original keys remain unchanged");
        for (int frame = 0; frame < 6; frame++)
            check(Files.isRegularFile(Paths.get(root + DecorResourcePaths.texture(
                "instruments_of_torture/pile_of_rotting_corpses", "_" + frame))), "animation frame " + frame);
        System.out.println("Decor model, particle atlas and animated texture paths passed");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
