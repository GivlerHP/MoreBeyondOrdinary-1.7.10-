package ru.givler.mbo.lootcontainer.action;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class LootContainerActionAdapter implements JsonSerializer<LootContainerAction>, JsonDeserializer<LootContainerAction> {
    @Override
    public JsonElement serialize(LootContainerAction src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject out = new JsonObject();
        out.addProperty("type", src.getType());
        src.writeTo(out);
        return out;
    }

    @Override
    public LootContainerAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = LootContainerAction.requireObject(json);
        String type = LootContainerAction.readString(object, "type", "").trim();
        if (type.isEmpty()) {
            throw new JsonParseException("Action type is empty.");
        }
        LootContainerAction action = LootContainerActionRegistry.create(type);
        if (action == null) {
            throw new JsonParseException("Unknown action type: " + type);
        }
        action.readFrom(object);
        return action;
    }
}
