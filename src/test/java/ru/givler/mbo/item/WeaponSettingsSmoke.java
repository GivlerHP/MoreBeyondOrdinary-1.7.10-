package ru.givler.mbo.item;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.common.collect.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import ru.givler.mbo.core.WeaponSettingsTransformer;

public final class WeaponSettingsSmoke {
    public static final class TestPlayer extends net.minecraft.entity.player.EntityPlayerMP {
        private UUID id;
        private TestPlayer(){super(null,null,null,null);}
        @Override public UUID getUniqueID(){return id;}
    }
    public static final class TestWorld extends net.minecraft.world.World {
        net.minecraft.tileentity.TileEntity tile;
        private TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null);}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
        public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z){return tile;}
        public int getBlockMetadata(int x,int y,int z){return 0;}
        public net.minecraft.block.Block getBlock(int x,int y,int z){return net.minecraft.init.Blocks.air;}
        public void markTileEntityChunkModified(int x,int y,int z,net.minecraft.tileentity.TileEntity tile){}
        public void func_147453_f(int x,int y,int z,net.minecraft.block.Block block){}
    }
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
        TestPlayer player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);
        player.id=UUID.randomUUID();player.capabilities=new net.minecraft.entity.player.PlayerCapabilities();
        ru.givler.mbo.config.LockSecurityConfig.protectionLevel=0;
        check(!ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEditWeapon(player),"Survival bypassed shared editor protection");
        check(ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEdit(player),"Public history must work in survival");
        player.capabilities.isCreativeMode=true;
        check(ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEditWeapon(player),"Creative default policy rejected");
        ru.givler.mbo.config.LockSecurityConfig.protectionLevel=2;
        check(!ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEditWeapon(player),"UUID policy ignored");
        Field list=ru.givler.mbo.config.LockSecurityConfig.class.getDeclaredField("allowedUuids");list.setAccessible(true);
        Set<String> allowed=(Set<String>)list.get(null);allowed.add(player.id.toString().replace("-",""));
        check(ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEditWeapon(player),"Whitelisted creative editor rejected");
        player.capabilities.isCreativeMode=false;
        check(!ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEditWeapon(player),"UUID must not bypass mandatory creative");
        ru.givler.mbo.config.WorkbenchConfig.publicHistory=false;
        check(!ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench.canEdit(player),"Private history accessible to survival");
        ru.givler.mbo.config.WorkbenchConfig.publicHistory=true;
        allowed.clear();ru.givler.mbo.config.LockSecurityConfig.protectionLevel=0;
        Item item=(Item)unsafe.allocateInstance(ru.givler.mbo.item.weapon.ItemWeaponBase.class);
        Field delegate=Item.class.getDeclaredField("delegate");unsafe.putObject(item,unsafe.objectFieldOffset(delegate),
                new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(item,Item.class));
        ItemStack stack=new ItemStack(item);stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setString("LoreOwner","unchanged");
        double[] values={17,400,125,200,10,60,30};WeaponSettings.write(stack,values);
        check(WeaponSettings.maxDamage(90,stack)==400,"Per-stack durability");
        check(Math.abs(WeaponSettings.ratio(new float[]{1,0,0},stack)[1]-.6)<.001,"MF2 ratio override");
        Multimap<String,AttributeModifier> attributes=HashMultimap.create();
        attributes.put("generic.attackDamage",new AttributeModifier(UUID.randomUUID(),"Original",2,0));
        attributes.put("generic.movementSpeed",new AttributeModifier(UUID.randomUUID(),"Other",.2,1));
        Multimap changed=WeaponSettings.attributes(attributes,stack);
        check(((AttributeModifier)changed.get("generic.attackDamage").iterator().next()).getAmount()==17,"Individual damage ignored");
        check(changed.get("generic.movementSpeed").size()==1 && attributes.get("generic.attackDamage").iterator().next().getAmount()==2,"Shared/default attributes mutated");
        check(WeaponSettings.original(stack).getTagCompound().getString("LoreOwner").equals("unchanged"),"Original snapshot lost unrelated NBT");
        check(!WeaponSettings.valid(new double[]{Double.NaN,1,100,100,1,0,0}),"NaN accepted");
        check(!WeaponSettings.valid(new double[]{1,1,100,100,0,0,0}),"Zero damage ratio accepted");
        WeaponSettings.reset(stack);check(!stack.getTagCompound().hasKey(WeaponSettings.KEY)&&stack.getTagCompound().getString("LoreOwner").equals("unchanged"),"Reset affected unrelated NBT");
        packetSecurity(unsafe,player,stack);
        verify("net.minecraft.item.ItemStack",2);
        verify("minefantasy.mf2.api.helpers.ArmourCalculator",1);
        System.out.println("Weapon settings passed: per-stack damage/durability/MF ratios, immutable defaults, reset, bounds and actual ItemStack/MF2 transformed bytecode");
    }
    private static void packetSecurity(sun.misc.Unsafe unsafe,TestPlayer player,ItemStack stack)throws Exception {
        cpw.mods.fml.common.Loader loader=(cpw.mods.fml.common.Loader)unsafe.allocateInstance(cpw.mods.fml.common.Loader.class);
        Field instance=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");instance.setAccessible(true);instance.set(null,loader);
        Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(loader,Collections.emptyMap());
        TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);
        ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench tile=new ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench();
        tile.setInventorySlotContents(0,stack);tile.setWorldObj(world);world.tile=tile;player.worldObj=world;
        player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);
        ru.givler.mbo.container.ContainerDescriptionWorkbench container=new ru.givler.mbo.container.ContainerDescriptionWorkbench(player.inventory,tile);
        container.windowId=9;player.openContainer=container;
        net.minecraft.network.NetHandlerPlayServer handler=(net.minecraft.network.NetHandlerPlayServer)unsafe.allocateInstance(net.minecraft.network.NetHandlerPlayServer.class);
        handler.playerEntity=player;
        Constructor<cpw.mods.fml.common.network.simpleimpl.MessageContext> constructor=cpw.mods.fml.common.network.simpleimpl.MessageContext.class
                .getDeclaredConstructor(net.minecraft.network.INetHandler.class,cpw.mods.fml.relauncher.Side.class);constructor.setAccessible(true);
        cpw.mods.fml.common.network.simpleimpl.MessageContext context=constructor.newInstance(handler,cpw.mods.fml.relauncher.Side.SERVER);
        Method drain=ru.givler.mbo.network.EditorPacketAccess.class.getDeclaredMethod("drainServer");drain.setAccessible(true);
        ru.givler.mbo.network.packet.PacketWeaponSettings.Handler receiver=new ru.givler.mbo.network.packet.PacketWeaponSettings.Handler();
        double[] values={20,400,100,100,0,100,0};
        player.capabilities.isCreativeMode=false;
        receiver.onMessage(new ru.givler.mbo.network.packet.PacketWeaponSettings(9,stack.copy(),values,false),context);drain.invoke(null);
        check(!stack.getTagCompound().hasKey(WeaponSettings.KEY),"Forged survival packet changed weapon");
        player.capabilities.isCreativeMode=true;
        receiver.onMessage(new ru.givler.mbo.network.packet.PacketWeaponSettings(8,stack.copy(),values,false),context);drain.invoke(null);
        check(!stack.getTagCompound().hasKey(WeaponSettings.KEY),"Wrong window accepted");
        ItemStack stale=stack.copy();stale.getTagCompound().setString("Other","stale");
        receiver.onMessage(new ru.givler.mbo.network.packet.PacketWeaponSettings(9,stale,values,false),context);drain.invoke(null);
        check(!stack.getTagCompound().hasKey(WeaponSettings.KEY),"Stale item accepted");
        player.posX=100;
        receiver.onMessage(new ru.givler.mbo.network.packet.PacketWeaponSettings(9,stack.copy(),values,false),context);drain.invoke(null);
        check(!stack.getTagCompound().hasKey(WeaponSettings.KEY),"Remote editor accepted");
        player.posX=0;
        receiver.onMessage(new ru.givler.mbo.network.packet.PacketWeaponSettings(9,stack.copy(),values,false),context);drain.invoke(null);
        check(stack.getTagCompound().getCompoundTag(WeaponSettings.KEY).getDouble("Damage")==20,"Authorized packet failed");
    }
    private static void verify(String type,int expected)throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try(InputStream input=WeaponSettingsSmoke.class.getResourceAsStream("/"+type.replace('.','/')+".class")) {
            byte[] bytes=new byte[8192];for(int count;(count=input.read(bytes))>=0;)out.write(bytes,0,count);
        }
        WeaponSettingsTransformer transformer=new WeaponSettingsTransformer();byte[] before=out.toByteArray(),after=transformer.transform(type,type,before);
        check(!Arrays.equals(before,after),"No override hooks in "+type);
        check(Arrays.equals(after,transformer.transform(type,type,after)),"Transformer not idempotent");
        ClassNode node=new ClassNode();new ClassReader(after).accept(node,0);Set<String> hooks=new HashSet<String>();
        for(MethodNode method:node.methods) {
            boolean patched=false;
            for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).owner.equals("ru/givler/mbo/item/WeaponSettings")){hooks.add(((MethodInsnNode)insn).name);patched=true;}
            if(patched)new Analyzer(new BasicVerifier()).analyze(node.name,method);
        }
        check(hooks.size()==expected,"Missing transformed methods: "+hooks);
    }
}
