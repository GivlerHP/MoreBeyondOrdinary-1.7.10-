package ru.givler.mbo.lootcontainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootContainerData {
    public static final int MAX_ACTIONS = 8;
    public static final int MAX_VARIATIONS = 5;
    public static final int MAX_CUSTOM_NAME_LENGTH = 30;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger("MBO.LootContainerData");

    public String customName = "";
    public List<ModelVariation> modelVariations = new ArrayList<ModelVariation>();
    public int recoveryTimeSec = 5;
    public boolean allowMultiaction = false;
    public boolean destroyOnPlayerCollide = true;
    public boolean destroyOnEntityCollide = false;
    public boolean destroyOnExplosion = false;
    public boolean destroyOnProjectileHit = false;
    public String actionsJson = "[]";

    public static class ModelVariation {
        public String normalModel = "";
        public String normalTexture = "";
        public String destroyedModel = "";
        public String destroyedTexture = "";
    }

    public List<LootContainerAction> getActions() {
        if (actionsJson == null || actionsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return LootContainerAction.fromJsonList(actionsJson);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse loot actions json: {}", actionsJson, ex);
            return Collections.emptyList();
        }
    }

    public void setActions(List<LootContainerAction> actions) {
        if (actions == null) {
            this.actionsJson = "[]";
            return;
        }
        List<LootContainerAction> clipped = actions;
        if (actions.size() > MAX_ACTIONS) {
            clipped = actions.subList(0, MAX_ACTIONS);
        }
        this.actionsJson = LootContainerAction.toJsonList(clipped);
    }

    public static LootContainerData fromItemStackNbt(NBTTagCompound tag) {
        LootContainerData data = new LootContainerData();
        if (tag == null) return data;

        data.customName = tag.getString("lc_customName");
        data.recoveryTimeSec = tag.hasKey("lc_recoverySec") ? tag.getInteger("lc_recoverySec") : 5;
        data.allowMultiaction = tag.getBoolean("lc_allowMultiaction");
        data.destroyOnPlayerCollide = !tag.hasKey("lc_destroyPlayerCollide") || tag.getBoolean("lc_destroyPlayerCollide");
        data.destroyOnEntityCollide = tag.getBoolean("lc_destroyEntityCollide");
        data.destroyOnExplosion = tag.hasKey("lc_destroyExplosion") && tag.getBoolean("lc_destroyExplosion");
        data.destroyOnProjectileHit = tag.getBoolean("lc_destroyProjectile");
        data.actionsJson = tag.hasKey("lc_actions") ? tag.getString("lc_actions") : "[]";
        data.modelVariations = parseModelVariations(tag.getString("lc_modelVariations"));
        return data;
    }

    public void writeToItemStackNbt(NBTTagCompound tag) {
        if (tag == null) return;
        tag.setString("lc_customName", limitName(customName));
        tag.setInteger("lc_recoverySec", Math.max(0, recoveryTimeSec));
        tag.setBoolean("lc_allowMultiaction", allowMultiaction);
        tag.setBoolean("lc_destroyPlayerCollide", destroyOnPlayerCollide);
        tag.setBoolean("lc_destroyEntityCollide", destroyOnEntityCollide);
        tag.setBoolean("lc_destroyExplosion", destroyOnExplosion);
        tag.setBoolean("lc_destroyProjectile", destroyOnProjectileHit);
        tag.setString("lc_actions", actionsJson == null ? "[]" : actionsJson);
        tag.setString("lc_modelVariations", serializeModelVariations(modelVariations));
    }

    public static List<ModelVariation> parseModelVariations(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<ModelVariation>();
        try {
            ModelVariation[] data = GSON.fromJson(json, ModelVariation[].class);
            List<ModelVariation> list = new ArrayList<ModelVariation>();
            if (data != null) {
                for (ModelVariation variation : data) {
                    if (variation == null) continue;
                    list.add(variation);
                    if (list.size() >= MAX_VARIATIONS) break;
                }
            }
            return list;
        } catch (JsonSyntaxException ex) {
            LOGGER.error("Failed to parse model variations json: {}", json, ex);
            return new ArrayList<ModelVariation>();
        }
    }

    public static String serializeModelVariations(List<ModelVariation> variations) {
        if (variations == null) return "[]";
        List<ModelVariation> clipped = variations;
        if (variations.size() > MAX_VARIATIONS) {
            clipped = variations.subList(0, MAX_VARIATIONS);
        }
        return GSON.toJson(clipped);
    }

    public static String limitName(String value) {
        if (value == null) return "";
        if (value.length() <= MAX_CUSTOM_NAME_LENGTH) return value;
        return value.substring(0, MAX_CUSTOM_NAME_LENGTH);
    }
}
