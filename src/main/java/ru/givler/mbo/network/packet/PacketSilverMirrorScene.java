package ru.givler.mbo.network.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.DataWatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import ru.givler.mbo.network.EditorPacketAccess;
import ru.givler.mbo.tileentity.TileSilverMirror;

public final class PacketSilverMirrorScene implements IMessage {
    private static final int MAX_ACTORS = 32;
    public int dimension, sourceX, sourceY, sourceZ, exitX, exitY, exitZ, facing;
    public boolean initial = true;
    public long streamId;
    public final List<Integer> changes = new ArrayList<>();
    public final List<S21PacketChunkData> chunks = new ArrayList<>();
    public static final int MAX_DECOR = 128;
    public List<NBTTagCompound> decor;
    public final List<Actor> actors = new ArrayList<>();

    public PacketSilverMirrorScene() {}
    public PacketSilverMirrorScene(int dimension, TileSilverMirror source, TileSilverMirror exit) {
        this.dimension = dimension;
        sourceX = source.xCoord; sourceY = source.yCoord; sourceZ = source.zCoord;
        exitX = exit.xCoord; exitY = exit.yCoord; exitZ = exit.zCoord;
        facing = exit.getBlockMetadata() & 3;
    }

    public void omitUnchangedAppearance(java.util.Map<Integer,Actor> previous) {
        java.util.Map<Integer,Actor> current = new java.util.HashMap<>();
        for (Actor actor : actors) {
            Actor old = previous.get(actor.id);
            current.put(actor.id,actor);
            if (old != null && old.type == actor.type) {
                actor.sendProfile = !actor.sameProfile(old);
                actor.sendEquipment = actor.sendProfile || !actor.sameEquipment(old);
            }
        }
        previous.clear();
        previous.putAll(current);
    }

    public void addChunk(S21PacketChunkData chunk) { chunks.add(chunk); }

    public void addChange(int index, int state) {
        changes.add(index);
        changes.add(state);
    }

    public void collectEntities(World world, TileSilverMirror exit) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(exitX-13,exitY-8,exitZ-13,exitX+14,exitY+10,exitZ+14);
        for (Object value : world.getEntitiesWithinAABB(EntityLivingBase.class,box)) {
            EntityLivingBase entity = (EntityLivingBase)value;
            if (!ru.givler.mbo.mirror.MirrorActors.isVisibleKind(entity) || entity.isDead || entity.isInvisible()) continue;
            int type = entity instanceof EntityPlayer ? 0 : EntityList.getEntityID(entity);
            if (type <= 0 && !(entity instanceof EntityPlayer)) continue;
            actors.add(new Actor(type,entity));
            if (actors.size() >= MAX_ACTORS) break;
        }
    }

    @Override public void toBytes(ByteBuf bytes) { encode(new PacketBuffer(bytes)); }
    @Override public void fromBytes(ByteBuf bytes) { decode(new PacketBuffer(bytes)); }

    private void encode(PacketBuffer out) {
        out.writeInt(dimension); out.writeInt(sourceX); out.writeInt(sourceY); out.writeInt(sourceZ);
        out.writeInt(exitX); out.writeInt(exitY); out.writeInt(exitZ); out.writeByte(facing);
        out.writeLong(streamId);
        out.writeBoolean(initial);
        out.writeByte(chunks.size());
        try {
            for (S21PacketChunkData chunk : chunks) chunk.writePacketData(out);
            out.writeShort(changes.size()/2);
            for (int i=0;i<changes.size();i+=2) {
                out.writeShort(changes.get(i));
                out.writeMedium(changes.get(i+1));
            }
            out.writeBoolean(decor!=null);
            if (decor!=null) {
                out.writeShort(decor.size());
                for (NBTTagCompound tag : decor) out.writeNBTTagCompoundToBuffer(tag);
            }
            out.writeByte(actors.size());
            for (Actor actor : actors) actor.write(out);
        } catch (IOException e) { throw new IllegalStateException("Could not encode mirror scene",e); }
    }

    private void decode(PacketBuffer in) {
        dimension=in.readInt(); sourceX=in.readInt(); sourceY=in.readInt(); sourceZ=in.readInt();
        exitX=in.readInt(); exitY=in.readInt(); exitZ=in.readInt(); facing=in.readUnsignedByte();
        streamId=in.readLong();
        initial=in.readBoolean();
        int count=in.readUnsignedByte();
        if (count > 9 || facing > 3) throw new IllegalArgumentException("Invalid mirror scene");
        try {
            for (int i=0;i<count;i++) {
                S21PacketChunkData chunk=new S21PacketChunkData();
                chunk.readPacketData(in);
                chunks.add(chunk);
            }
            count=in.readUnsignedShort();
            if (count > ru.givler.mbo.mirror.SilverMirrorBlocks.SIZE) throw new IllegalArgumentException("Too many mirror block changes");
            for (int i=0;i<count;i++) addChange(in.readUnsignedShort(),in.readUnsignedMedium());
            if (in.readBoolean()) {
                count=in.readUnsignedShort();
                if (count>MAX_DECOR) throw new IllegalArgumentException("Too many mirror models");
                decor=new ArrayList<>(count);
                for (int i=0;i<count;i++) decor.add(in.readNBTTagCompoundFromBuffer());
            }
            count=in.readUnsignedByte();
            if (count > MAX_ACTORS) throw new IllegalArgumentException("Too many mirror actors");
            for (int i=0;i<count;i++) actors.add(new Actor(in));
        } catch (IOException e) { throw new IllegalArgumentException("Could not decode mirror scene",e); }
    }

    public static final class Actor {
        public final int id,type;
        public final GameProfile profile;
        public final double x,y,z;
        public final float yaw,pitch,head;
        public final boolean child;
        public final List<?> watched;
        public final ItemStack[] equipment = new ItemStack[5];
        public boolean sendProfile = true;
        public boolean sendEquipment = true;
        public final float swing;
        public final int useTicks;

        Actor(int type, EntityLivingBase entity) {
            this.type=type;
            id=entity.getEntityId();
            profile=type==0 ? copyProfile(((EntityPlayer)entity).getGameProfile()) : null;
            x=entity.posX; y=entity.posY; z=entity.posZ;
            yaw=entity.renderYawOffset; pitch=entity.rotationPitch; head=entity.rotationYawHead;
            child=entity instanceof EntityAgeable && entity.isChild();
            watched=entity.getDataWatcher().getAllWatched();
            for (int slot=0;slot<equipment.length;slot++) {
                ItemStack stack=entity.getEquipmentInSlot(slot);
                equipment[slot]=stack==null ? null : stack.copy();
            }
            swing=entity.swingProgress;
            useTicks=entity instanceof EntityPlayer ? ((EntityPlayer)entity).getItemInUseCount() : 0;
        }

        Actor(PacketBuffer in) throws IOException {
            id=in.readInt();
            type=in.readInt();
            int flags=in.readUnsignedByte();
            sendProfile=(flags&1)!=0;
            sendEquipment=(flags&2)!=0;
            profile=type==0 && sendProfile
                ? new GameProfile(new UUID(in.readLong(),in.readLong()),in.readStringFromBuffer(16)) : null;
            if (profile!=null && in.readBoolean()) {
                String texture=in.readStringFromBuffer(32767);
                String signature=in.readBoolean()?in.readStringFromBuffer(32767):null;
                profile.getProperties().put("textures",new Property("textures",texture,signature));
            }
            x=in.readDouble(); y=in.readDouble(); z=in.readDouble();
            yaw=in.readFloat(); pitch=in.readFloat(); head=in.readFloat();
            child=in.readBoolean();
            watched=DataWatcher.readWatchedListFromPacketBuffer(in);
            if (sendEquipment) {
                for (int slot=0;slot<equipment.length;slot++) equipment[slot]=in.readItemStackFromBuffer();
            }
            swing=in.readFloat();
            useTicks=in.readVarIntFromBuffer();
        }

        private static GameProfile copyProfile(GameProfile input) {
            GameProfile copy=new GameProfile(input.getId(),input.getName());
            copy.getProperties().putAll("textures",input.getProperties().get("textures"));
            return copy;
        }

        private boolean sameProfile(Actor old) {
            if (profile==null) return old.profile==null;
            if (!profile.equals(old.profile)) return false;
            java.util.Iterator<Property> a=profile.getProperties().get("textures").iterator();
            java.util.Iterator<Property> b=old.profile.getProperties().get("textures").iterator();
            if (!a.hasNext() || !b.hasNext()) return a.hasNext()==b.hasNext();
            Property current=a.next(), previous=b.next();
            return current.getValue().equals(previous.getValue())
                && java.util.Objects.equals(current.getSignature(),previous.getSignature());
        }

        private boolean sameEquipment(Actor old) {
            for (int slot=0;slot<equipment.length;slot++) {
                if (!ItemStack.areItemStacksEqual(equipment[slot],old.equipment[slot])) return false;
            }
            return true;
        }

        void write(PacketBuffer out) throws IOException {
            out.writeInt(id);
            out.writeInt(type);
            out.writeByte((sendProfile?1:0)|(sendEquipment?2:0));
            if (type==0 && sendProfile) {
                out.writeLong(profile.getId().getMostSignificantBits());
                out.writeLong(profile.getId().getLeastSignificantBits());
                out.writeStringToBuffer(profile.getName());
                java.util.Iterator<Property> textures=profile.getProperties().get("textures").iterator();
                boolean textured=textures.hasNext();
                out.writeBoolean(textured);
                if (textured) {
                    Property texture=textures.next();
                    out.writeStringToBuffer(texture.getValue());
                    out.writeBoolean(texture.hasSignature());
                    if (texture.hasSignature()) out.writeStringToBuffer(texture.getSignature());
                }
            }
            out.writeDouble(x); out.writeDouble(y); out.writeDouble(z);
            out.writeFloat(yaw); out.writeFloat(pitch); out.writeFloat(head); out.writeBoolean(child);
            DataWatcher.writeWatchedListToPacketBuffer(watched,out);
            if (sendEquipment) for (ItemStack stack : equipment) out.writeItemStackToBuffer(stack);
            out.writeFloat(swing);
            out.writeVarIntToBuffer(useTicks);
        }
    }

    public static final class Handler implements IMessageHandler<PacketSilverMirrorScene,IMessage> {
        @Override public IMessage onMessage(PacketSilverMirrorScene message,MessageContext context) {
            EditorPacketAccess.scheduleClient(() ->
                ru.givler.mbo.client.render.SilverMirrorView.receive(message));
            return null;
        }
    }
}
