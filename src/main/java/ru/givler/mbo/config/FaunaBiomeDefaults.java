package ru.givler.mbo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.common.config.Configuration;

/** Exact biome names from the approved server biome list; IDs are intentionally not used. */
public final class FaunaBiomeDefaults {
  private static final Map<String, Habitat> DEFAULTS;
  private static Map<String, Habitat> HABITATS;

  static {
    InputStream input =
        FaunaBiomeDefaults.class.getResourceAsStream(
            "/assets/mbo/fauna/biome_habitats.default.json");
    if (input == null) throw new IllegalStateException("Missing fauna biome defaults");
    try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
      Type type = new TypeToken<Map<String, Habitat>>() {}.getType();
      DEFAULTS = new Gson().fromJson(reader, type);
      HABITATS = DEFAULTS;
    } catch (IOException error) {
      throw new IllegalStateException("Cannot read fauna biome defaults", error);
    }
  }

  private FaunaBiomeDefaults() {}

  /** The packaged resource is only a first-run template; the external file is authoritative. */
  public static void load(File directory, Configuration config) {
    File file = new File(directory, "MoreBeyondOrdinary/biome_habitats.json");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Type type = new TypeToken<Map<String, Habitat>>() {}.getType();
    try {
      Files.createDirectories(file.toPath().getParent());
      if (!file.exists()) {
        Map<String, Habitat> initial = gson.fromJson(gson.toJson(DEFAULTS), type);
        for (String id : initial.keySet()) {
          Habitat habitat = initial.get(id), defaults = DEFAULTS.get(id);
          for (String key : new String[] {"surfaceBiomes", "caveBiomes"}) {
            if (!config.hasKey(id, key)) continue;
            String[] old = config.getCategory(id).get(key).getStringList();
            String[] original = key.equals("surfaceBiomes") ? defaults.surface : defaults.caves;
            if (!Arrays.equals(old, original)) {
              if (key.equals("surfaceBiomes")) habitat.surface = old;
              else habitat.caves = old;
            }
          }
        }
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
          gson.toJson(initial, type, writer);
        }
      }
      Map<String, Habitat> loaded;
      try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
        loaded = gson.fromJson(reader, type);
      }
      if (loaded == null) throw new IllegalArgumentException("Empty biome habitat file");
      for (String id : DEFAULTS.keySet()) {
        Habitat habitat = loaded.get(id);
        if (habitat == null || habitat.surface == null || habitat.caves == null)
          throw new IllegalArgumentException("Missing surface/caves arrays for " + id);
        for (String[] names : new String[][] {habitat.surface, habitat.caves})
          for (String name : names)
            if (name == null || name.trim().isEmpty())
              throw new IllegalArgumentException("Invalid biome name for " + id);
      }
      // Remove duplicated generated selectors; explicit whitelist/blacklist overrides remain.
      if (config.getInt(
              "habitatFileVersion", "general", 0, 0, 1, "External habitat file migration.")
          == 0) {
        for (String id : DEFAULTS.keySet()) {
          Habitat original = DEFAULTS.get(id);
          Set<String> names = new LinkedHashSet<String>();
          Collections.addAll(names, original.surface);
          Collections.addAll(names, original.caves);
          if (config.hasKey(id, "biomeWhitelist")) {
            String[] whitelist = config.getCategory(id).get("biomeWhitelist").getStringList();
            if (names.equals(new LinkedHashSet<String>(Arrays.asList(whitelist))))
              config.getCategory(id).get("biomeWhitelist").set(new String[0]);
          }
          config.getCategory(id).remove("surfaceBiomes");
          config.getCategory(id).remove("caveBiomes");
        }
        config.get("general", "habitatFileVersion", 1).set(1);
      }
      HABITATS = loaded;
    } catch (IOException | RuntimeException error) {
      throw new IllegalStateException(
          "Cannot load fauna habitats: " + file.getAbsolutePath(), error);
    }
  }

  public static String[] surface(String id) {
    return habitat(id).surface.clone();
  }

  public static String[] caves(String id) {
    return habitat(id).caves.clone();
  }

  public static String[] all(String id) {
    Set<String> names = new LinkedHashSet<String>();
    Collections.addAll(names, surface(id));
    Collections.addAll(names, caves(id));
    return names.toArray(new String[names.size()]);
  }

  private static Habitat habitat(String id) {
    Habitat habitat = HABITATS.get(id);
    if (habitat == null) throw new IllegalArgumentException("Unknown fauna species: " + id);
    return habitat;
  }

  private static final class Habitat {
    String[] surface, caves;
  }
}
