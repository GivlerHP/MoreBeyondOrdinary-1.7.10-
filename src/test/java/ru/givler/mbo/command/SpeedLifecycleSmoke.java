package ru.givler.mbo.command;

import cpw.mods.fml.common.gameevent.PlayerEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.UUID;
import net.minecraft.entity.player.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.*;

public final class SpeedLifecycleSmoke {
    public static void main(String[] args) throws Exception {
        Field singleton=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        FakePlayer player=(FakePlayer)((sun.misc.Unsafe)singleton.get(null)).allocateInstance(FakePlayer.class);
        EntityPlayer.class.getField("capabilities").set(player,new PlayerCapabilities());
        SpeedEventHandler handler=new SpeedEventHandler();
        handler.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        speeds(player,.1F,.05F);
        Method settingsMethod=CommandSpeed.class.getDeclaredMethod("getSettings",EntityPlayerMP.class);
        settingsMethod.setAccessible(true);Object settings=settingsMethod.invoke(null,player);
        for(String name:new String[]{"walk","fly"}) {
            Field field=settings.getClass().getDeclaredField(name);field.setAccessible(true);
            field.setFloat(settings,name.equals("walk")?3F:4F);
        }
        CommandSpeed.apply(player);speeds(player,.3F,.2F);
        handler.onRespawn(new PlayerEvent.PlayerRespawnEvent(player));speeds(player,.3F,.2F);
        if(player.syncs!=3)throw new AssertionError("Abilities must synchronize at login, apply and respawn");
        handler.onLogout(new PlayerEvent.PlayerLoggedOutEvent(player));speeds(player,.1F,.05F);
        CommandSpeed.apply(player);speeds(player,.1F,.05F);
        handler.onLogout(new PlayerEvent.PlayerLoggedOutEvent(player));speeds(player,.1F,.05F);
        strippedCapabilities(false);strippedCapabilities(true);
        System.out.println("SPEED_LIFECYCLE_OK: login, custom walk/fly, respawn, logout, cleared settings; setters absent with MCP/SRG fields");
    }
    private static void speeds(FakePlayer player,float walk,float fly) {
        if(Math.abs(player.capabilities.getWalkSpeed()-walk)>.00001F
                || Math.abs(player.capabilities.getFlySpeed()-fly)>.00001F)throw new AssertionError("Movement speed changed");
    }
    private static void strippedCapabilities(boolean srg) throws Exception {
        String caps="net.minecraft.entity.player.PlayerCapabilities",access=PlayerSpeedAccess.class.getName();
        ClassNode node=new ClassNode();new ClassReader(bytes(caps)).accept(node,0);
        // These two setters are removed by Forge on a dedicated server.
        node.methods.removeIf(method->method.name.equals("setPlayerWalkSpeed")||method.name.equals("setFlySpeed"));
        ClassWriter writer=new ClassWriter(0);node.accept(writer);byte[] input=writer.toByteArray();
        if(srg) {
            writer=new ClassWriter(0);
            new ClassReader(input).accept(new RemappingClassAdapter(writer,new Remapper(){
                public String mapFieldName(String owner,String name,String desc) {
                    return name.equals("walkSpeed")?"field_75097_g":name.equals("flySpeed")?"field_75096_f":name;
                }
            }),ClassReader.EXPAND_FRAMES);input=writer.toByteArray();
        }
        final byte[] capabilityBytes=input,accessBytes=bytes(access);
        ClassLoader loader=new ClassLoader(SpeedLifecycleSmoke.class.getClassLoader()) {
            protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
                synchronized(getClassLoadingLock(name)) {
                    if(!name.equals(caps)&&!name.equals(access))return super.loadClass(name,resolve);
                    Class<?> type=findLoadedClass(name);
                    if(type==null){byte[] data=name.equals(caps)?capabilityBytes:accessBytes;type=defineClass(name,data,0,data.length);}
                    if(resolve)resolveClass(type);return type;
                }
            }
        };
        Class<?> type=loader.loadClass(caps);Object capabilities=type.newInstance();
        Method set=loader.loadClass(access).getDeclaredMethod("set",type,float.class,float.class);set.setAccessible(true);
        set.invoke(null,capabilities,.6F,.25F);
        for(String fieldName:new String[]{srg?"field_75097_g":"walkSpeed",srg?"field_75096_f":"flySpeed"}) {
            Field field=type.getDeclaredField(fieldName);field.setAccessible(true);
            float expected=fieldName.equals("walkSpeed")||fieldName.equals("field_75097_g")?.6F:.25F;
            if(field.getFloat(capabilities)!=expected)throw new AssertionError("Dedicated-server speed write: "+fieldName);
        }
    }
    private static byte[] bytes(String name)throws IOException {
        try(InputStream input=SpeedLifecycleSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
            ByteArrayOutputStream output=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
            while((count=input.read(buffer))!=-1)output.write(buffer,0,count);return output.toByteArray();
        }
    }
    public static final class FakePlayer extends EntityPlayerMP {
        int syncs;
        public FakePlayer(){super(null,null,null,null);}
        public UUID getUniqueID(){return new UUID(0,42);}
        public void sendPlayerAbilities(){syncs++;}
    }
}
