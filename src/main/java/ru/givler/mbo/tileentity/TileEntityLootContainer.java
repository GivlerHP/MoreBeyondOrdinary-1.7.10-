package ru.givler.mbo.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;

import java.util.ArrayList;
import java.util.List;

public class TileEntityLootContainer extends ModelTileBase {
    private static final String FALLBACK_MODEL = "jugs";
    private static final String FALLBACK_TEXTURE = "jugs";
    private static final String MISSING_DESTROYED_TEXTURE = "lootcontainer_missing";

    private boolean destroyed = false;
    private long destroyedAtEpochSec = 0L;
    private int recoveryTimeSec = 5;
    private boolean allowMultiaction = false;
    private boolean destroyOnPlayerCollide = true;
    private boolean destroyOnEntityCollide = false;
    private boolean destroyOnExplosion = false;
    private boolean destroyOnProjectileHit = false;
    private String customName = "";
    private String actionsJson = "[]";
    private String modelVariationsJson = "[]";

    private String normalModel = FALLBACK_MODEL;
    private String normalTexture = FALLBACK_TEXTURE;
    private String destroyedModel = "";
    private String destroyedTexture = "";
    private boolean destroyedFallbackMissing = false;

    public TileEntityLootContainer() {
        super(FALLBACK_TEXTURE, FALLBACK_MODEL);
    }

    public void applyConfig(LootContainerData data, boolean chooseRandomVariation) {
        if (data == null) return;

        this.customName = LootContainerData.limitName(data.customName);
        this.recoveryTimeSec = Math.max(0, data.recoveryTimeSec);
        this.allowMultiaction = data.allowMultiaction;
        this.destroyOnPlayerCollide = data.destroyOnPlayerCollide;
        this.destroyOnEntityCollide = data.destroyOnEntityCollide;
        this.destroyOnExplosion = data.destroyOnExplosion;
        this.destroyOnProjectileHit = data.destroyOnProjectileHit;
        this.actionsJson = data.actionsJson == null ? "[]" : data.actionsJson;
        this.modelVariationsJson = LootContainerData.serializeModelVariations(data.modelVariations);

        LootContainerData.ModelVariation selected = null;
        if (data.modelVariations != null && !data.modelVariations.isEmpty()) {
            int index = 0;
            if (chooseRandomVariation && data.modelVariations.size() > 1) {
                index = worldObj != null ? worldObj.rand.nextInt(data.modelVariations.size()) : 0;
            }
            selected = data.modelVariations.get(index);
        }

        if (selected != null && selected.normalModel != null && !selected.normalModel.isEmpty()) {
            this.normalModel = selected.normalModel;
            this.normalTexture = (selected.normalTexture == null || selected.normalTexture.isEmpty())
                    ? FALLBACK_TEXTURE : selected.normalTexture;
        } else {
            this.normalModel = FALLBACK_MODEL;
            this.normalTexture = FALLBACK_TEXTURE;
        }

        if (selected != null && selected.destroyedModel != null && !selected.destroyedModel.isEmpty()) {
            this.destroyedModel = selected.destroyedModel;
            this.destroyedTexture = (selected.destroyedTexture == null || selected.destroyedTexture.isEmpty())
                    ? MISSING_DESTROYED_TEXTURE : selected.destroyedTexture;
            this.destroyedFallbackMissing = false;
        } else {
            this.destroyedModel = FALLBACK_MODEL;
            this.destroyedTexture = MISSING_DESTROYED_TEXTURE;
            this.destroyedFallbackMissing = true;
        }

        applyModelForCurrentState();
        sync();
    }

    public boolean tryDestroy(Entity source) {
        return tryDestroy(source, true);
    }

    public boolean tryDestroy(Entity source, boolean spawnBreakParticles) {
        if (destroyed) return false;
        destroyed = true;
        destroyedAtEpochSec = System.currentTimeMillis() / 1000L;
        applyModelForCurrentState();
        if (spawnBreakParticles) {
            spawnBreakParticles();
        }
        executeActions(source);
        sync();
        return true;
    }

    public boolean canRenderFor(EntityPlayer player) {
        if (!destroyed) return true;
        if (!destroyedFallbackMissing) return true;
        return player != null && player.capabilities.isCreativeMode;
    }

    public void restoreNow() {
        if (!destroyed && destroyedAtEpochSec == 0L) return;
        destroyed = false;
        destroyedAtEpochSec = 0L;
        applyModelForCurrentState();
        sync();
    }

    private void executeActions(Entity source) {
        List<LootContainerAction> actions = parseActions();
        if (actions.isEmpty() || worldObj == null || worldObj.isRemote) return;

        if (allowMultiaction) {
            List<LootContainerAction> passed = new ArrayList<LootContainerAction>();
            for (LootContainerAction action : actions) {
                float chance = clampChance(action.getChance());
                int roll = worldObj.rand.nextInt(100);
                if (roll >= (100.0F - chance)) {
                    passed.add(action);
                }
            }
            if (passed.isEmpty()) return;
            for (LootContainerAction action : passed) {
                action.execute(this, source);
            }
            return;
        }

        List<LootContainerAction> weighted = new ArrayList<LootContainerAction>();
        float totalWeight = 0.0F;
        for (LootContainerAction action : actions) {
            float weight = action == null ? 0.0F : Math.max(0.0F, action.getChance());
            if (weight <= 0.0F) continue;
            weighted.add(action);
            totalWeight += weight;
        }
        if (weighted.isEmpty() || totalWeight <= 0.0F) return;

        float roll = worldObj.rand.nextFloat() * totalWeight;
        float accumulated = 0.0F;
        for (LootContainerAction action : weighted) {
            accumulated += Math.max(0.0F, action.getChance());
            if (roll < accumulated) {
                action.execute(this, source);
                return;
            }
        }
        weighted.get(weighted.size() - 1).execute(this, source);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        if (!destroyed || recoveryTimeSec <= 0) return;
        long now = System.currentTimeMillis() / 1000L;
        if (now - destroyedAtEpochSec >= recoveryTimeSec) {
            destroyed = false;
            destroyedAtEpochSec = 0L;
            applyModelForCurrentState();
            sync();
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean shouldDestroyOnPlayerCollide() {
        return destroyOnPlayerCollide;
    }

    public boolean shouldDestroyOnEntityCollide() {
        return destroyOnEntityCollide;
    }

    public boolean shouldDestroyOnExplosion() {
        return destroyOnExplosion;
    }

    public boolean shouldDestroyOnProjectileHit() {
        return destroyOnProjectileHit;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean hasConfiguredVariations() {
        List<LootContainerData.ModelVariation> list = LootContainerData.parseModelVariations(modelVariationsJson);
        return !list.isEmpty();
    }

    public List<LootContainerAction> parseActions() {
        LootContainerData data = new LootContainerData();
        data.actionsJson = actionsJson;
        List<LootContainerAction> actions = data.getActions();
        if (actions.size() > LootContainerData.MAX_ACTIONS) {
            return actions.subList(0, LootContainerData.MAX_ACTIONS);
        }
        return actions;
    }

    public LootContainerData exportConfig() {
        LootContainerData data = new LootContainerData();
        data.customName = customName;
        data.recoveryTimeSec = recoveryTimeSec;
        data.allowMultiaction = allowMultiaction;
        data.destroyOnPlayerCollide = destroyOnPlayerCollide;
        data.destroyOnEntityCollide = destroyOnEntityCollide;
        data.destroyOnExplosion = destroyOnExplosion;
        data.destroyOnProjectileHit = destroyOnProjectileHit;
        data.actionsJson = actionsJson == null ? "[]" : actionsJson;
        data.modelVariations = LootContainerData.parseModelVariations(modelVariationsJson);
        return data;
    }

    private void applyModelForCurrentState() {
        if (destroyed) {
            this.modelName = destroyedModel == null || destroyedModel.isEmpty() ? FALLBACK_MODEL : destroyedModel;
            this.textureName = destroyedTexture == null || destroyedTexture.isEmpty() ? MISSING_DESTROYED_TEXTURE : destroyedTexture;
        } else {
            this.modelName = normalModel == null || normalModel.isEmpty() ? FALLBACK_MODEL : normalModel;
            this.textureName = normalTexture == null || normalTexture.isEmpty() ? FALLBACK_TEXTURE : normalTexture;
        }
    }

    private void spawnBreakParticles() {
        if (worldObj == null) return;
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        worldObj.playAuxSFX(2001, xCoord, yCoord, zCoord,
                net.minecraft.block.Block.getIdFromBlock(getBlockType()) + (meta << 12));
    }

    private void sync() {
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private static float clampChance(float chance) {
        if (chance < 0F) return 0F;
        if (chance > 100F) return 100F;
        return chance;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("lc_destroyed", destroyed);
        tag.setLong("lc_destroyedAt", destroyedAtEpochSec);
        tag.setInteger("lc_recoverySec", recoveryTimeSec);
        tag.setBoolean("lc_allowMultiaction", allowMultiaction);
        tag.setBoolean("lc_destroyPlayerCollide", destroyOnPlayerCollide);
        tag.setBoolean("lc_destroyEntityCollide", destroyOnEntityCollide);
        tag.setBoolean("lc_destroyExplosion", destroyOnExplosion);
        tag.setBoolean("lc_destroyProjectile", destroyOnProjectileHit);
        tag.setString("lc_customName", customName == null ? "" : customName);
        tag.setString("lc_actions", actionsJson == null ? "[]" : actionsJson);
        tag.setString("lc_modelVariations", modelVariationsJson == null ? "[]" : modelVariationsJson);
        tag.setString("lc_normalModel", normalModel == null ? FALLBACK_MODEL : normalModel);
        tag.setString("lc_normalTexture", normalTexture == null ? FALLBACK_TEXTURE : normalTexture);
        tag.setString("lc_destroyedModel", destroyedModel == null ? "" : destroyedModel);
        tag.setString("lc_destroyedTexture", destroyedTexture == null ? "" : destroyedTexture);
        tag.setBoolean("lc_destroyedFallbackMissing", destroyedFallbackMissing);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        destroyed = tag.getBoolean("lc_destroyed");
        destroyedAtEpochSec = tag.getLong("lc_destroyedAt");
        recoveryTimeSec = tag.getInteger("lc_recoverySec");
        allowMultiaction = tag.getBoolean("lc_allowMultiaction");
        destroyOnPlayerCollide = !tag.hasKey("lc_destroyPlayerCollide") || tag.getBoolean("lc_destroyPlayerCollide");
        destroyOnEntityCollide = tag.getBoolean("lc_destroyEntityCollide");
        destroyOnExplosion = tag.hasKey("lc_destroyExplosion") && tag.getBoolean("lc_destroyExplosion");
        destroyOnProjectileHit = tag.getBoolean("lc_destroyProjectile");
        customName = tag.getString("lc_customName");
        actionsJson = tag.hasKey("lc_actions") ? tag.getString("lc_actions") : "[]";
        modelVariationsJson = tag.hasKey("lc_modelVariations") ? tag.getString("lc_modelVariations") : "[]";
        normalModel = tag.hasKey("lc_normalModel") ? tag.getString("lc_normalModel") : FALLBACK_MODEL;
        normalTexture = tag.hasKey("lc_normalTexture") ? tag.getString("lc_normalTexture") : FALLBACK_TEXTURE;
        destroyedModel = tag.getString("lc_destroyedModel");
        destroyedTexture = tag.getString("lc_destroyedTexture");
        destroyedFallbackMissing = tag.getBoolean("lc_destroyedFallbackMissing");
        applyModelForCurrentState();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        if (pkt != null && pkt.func_148857_g() != null) {
            readFromNBT(pkt.func_148857_g());
        }
    }
}
