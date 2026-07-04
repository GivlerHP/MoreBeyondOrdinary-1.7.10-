package ru.givler.mbo.lootcontainer.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.Entity;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LootContainerAction {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(LootContainerAction.class, new LootContainerActionAdapter())
            .create();

    private float chance = 100.0F;

    public abstract String getType();

    public abstract void execute(TileEntityLootContainer tile, Entity source);

    protected abstract void writeFields(JsonObject json);

    protected abstract void readFields(JsonObject json);

    public final float getChance() {
        return chance;
    }

    public final void setChance(float chance) {
        this.chance = Math.max(0.0F, chance);
    }

    final void writeTo(JsonObject json) {
        json.addProperty("chance", getChance());
        writeFields(json);
    }

    final void readFrom(JsonObject json) {
        setChance(readFloat(json, "chance", 100.0F));
        readFields(json);
    }

    public final String toJson() {
        return GSON.toJson(this, LootContainerAction.class);
    }

    public static LootContainerAction fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new JsonParseException("Action json is empty.");
        }
        LootContainerAction action = GSON.fromJson(json, LootContainerAction.class);
        if (action == null) {
            throw new JsonParseException("Action json returned null.");
        }
        return action;
    }

    public static String toJsonList(List<LootContainerAction> actions) {
        if (actions == null) return "[]";
        JsonArray arr = new JsonArray();
        for (LootContainerAction action : actions) {
            if (action == null) continue;
            arr.add(GSON.toJsonTree(action, LootContainerAction.class));
        }
        return GSON.toJson(arr);
    }

    public static List<LootContainerAction> fromJsonList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        JsonElement root = GSON.fromJson(json, JsonElement.class);
        if (root == null || root.isJsonNull()) {
            return Collections.emptyList();
        }
        if (!root.isJsonArray()) {
            throw new JsonParseException("Actions json must be array.");
        }
        JsonArray arr = root.getAsJsonArray();
        if (arr.size() == 0) return Collections.emptyList();

        List<LootContainerAction> out = new ArrayList<LootContainerAction>(arr.size());
        for (JsonElement element : arr) {
            if (element == null || element.isJsonNull()) continue;
            LootContainerAction action = GSON.fromJson(element, LootContainerAction.class);
            if (action != null) out.add(action);
        }
        return out;
    }

    static String readString(JsonObject json, String key, String fallback) {
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsString();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static int readInt(JsonObject json, String key, int fallback) {
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static float readFloat(JsonObject json, String key, float fallback) {
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsFloat();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static boolean readBoolean(JsonObject json, String key, boolean fallback) {
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) return fallback;
        try {
            return el.getAsBoolean();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static JsonObject requireObject(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException("Action json must be an object.");
        }
        return element.getAsJsonObject();
    }
}
