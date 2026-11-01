package ru.givler.mbo.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/** Stop species AI before it can feed, attack or alter terrain; reuse native camera streaming. */
public final class PossessionTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/magic/possession/PossessionManager";
  private static final String CLIENT="ru/givler/mbo/client/handler/PossessionClient";
  public byte[] transform(String name,String mapped,byte[] bytes) {
    if(bytes==null)return null;
    boolean living=mapped.equals("net.minecraft.entity.EntityLivingBase")||mapped.startsWith("net.minecraft.entity.passive.")
        ||mapped.startsWith("ru.givler.mbo.entity.fauna.Entity");
    boolean player=mapped.equals("net.minecraft.network.NetHandlerPlayServer");
    boolean mouse=mapped.equals("net.minecraft.entity.Entity");
    boolean sound=mapped.equals("net.minecraft.client.audio.SoundManager");
    boolean broadcast=mapped.equals("net.minecraft.server.management.ServerConfigurationManager");
    boolean activeWorld=mapped.equals("net.minecraft.world.World");
    boolean chunkOrder=mapped.equals("net.minecraftforge.common.util.ChunkCoordComparator");
    boolean input=mapped.equals("net.minecraft.util.MovementInputFromOptions");
    boolean equine=mapped.equals("net.minecraft.entity.passive.EntityHorse")||mapped.equals("ru.givler.mbo.entity.fauna.EntityMBOCamel");
    boolean mfHorse=mapped.equals("minefantasy.mf2.mechanics.HorseMovement");
    boolean mfPending=mapped.equals("minefantasy.mf2.network.packet.PacketHorseMove$Pending");
    boolean stream=mapped.equals("net.minecraft.server.management.PlayerManager")||mapped.equals("net.minecraft.entity.EntityTrackerEntry");
    if(!living&&!player&&!stream&&!mouse&&!sound&&!broadcast&&!activeWorld&&!chunkOrder&&!input&&!mfHorse&&!mfPending)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    String api="ru/givler/mbo/magic/possession/PossessionAnimal";
    if(mapped.equals("net.minecraft.entity.EntityLivingBase")&&!node.interfaces.contains(api)) {
      java.util.List<MethodNode> accessors=new java.util.ArrayList<MethodNode>();
      for(MethodNode method:node.methods) {
        String nameMapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
        boolean blocked=method.desc.equals("()Z")&&(method.name.equals("isMovementBlocked")||method.name.equals("func_70610_aX")||nameMapped.equals("func_70610_aX"));
        boolean jump=method.desc.equals("()V")&&(method.name.equals("jump")||method.name.equals("func_70664_aZ")||nameMapped.equals("func_70664_aZ"));
        if(blocked||jump){MethodNode accessor=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,blocked?"mbo$movementBlocked":"mbo$jump",method.desc,null,null);accessor.instructions.add(new VarInsnNode(ALOAD,0));accessor.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,node.name,method.name,method.desc,false));accessor.instructions.add(new InsnNode(blocked?IRETURN:RETURN));accessors.add(accessor);}
      }
      if(accessors.size()!=2)throw new IllegalStateException("Native possession movement accessors missing");
      node.interfaces.add(api);node.methods.addAll(accessors);count+=2;
    }
    for(MethodNode method:node.methods) {
      boolean patched=false;for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode&&(((MethodInsnNode)ins).owner.equals(HOOK)||((MethodInsnNode)ins).owner.equals(CLIENT)))patched=true;
      if(patched)continue;
      String m=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
      if(mapped.equals("net.minecraft.entity.EntityLivingBase")&&method.desc.equals("(FF)V")&&(method.name.equals("moveEntityWithHeading")||method.name.equals("func_70612_e")||m.equals("func_70612_e"))){guard(method,0,"clientBatMovement","(Lnet/minecraft/entity/EntityLivingBase;)Z",false);count++;}
      if(equine&&method.desc.equals("(FF)V")&&(method.name.equals("moveEntityWithHeading")||method.name.equals("func_70612_e")||m.equals("func_70612_e"))){guard(method,0,"clientHorse","(Lnet/minecraft/entity/passive/EntityHorse;)Z",false);count++;}
      if(equine&&method.desc.equals("(I)V")&&(method.name.equals("setJumpPower")||method.name.equals("func_110206_u")||m.equals("func_110206_u"))){guard(method,0,"blockRiderJump","(Lnet/minecraft/entity/passive/EntityHorse;)Z",false);count++;}
      if(mfHorse&&method.name.equals("localDriver")){guard(method,0,"isControlled","(Lnet/minecraft/entity/Entity;)Z",true);count++;}
      if(mfPending&&method.name.equals("apply")&&method.desc.equals("()V")) {
        FieldNode playerField=null;for(FieldNode field:node.fields)if(field.name.equals("player"))playerField=field;
        if(playerField!=null){InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,node.name,playerField.name,playerField.desc));hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"passenger","(Lnet/minecraft/entity/Entity;)Z",false));hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;}
      }
      if(input&&method.desc.equals("()V")&&(method.name.equals("updatePlayerMoveState")||method.name.equals("func_78898_a")||m.equals("func_78898_a"))) {
        for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==RETURN){InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,0));hook.add(new MethodInsnNode(INVOKESTATIC,CLIENT,"suppressBodyInput","(Lnet/minecraft/util/MovementInput;)V",false));method.instructions.insertBefore(ins,hook);count++;}
      }
      if(sound&&(method.name.equals("setListener")||method.name.equals("func_148615_a")||m.equals("func_148615_a"))) {
        InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,1));hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"listener","(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/entity/player/EntityPlayer;",false));hook.add(new VarInsnNode(ASTORE,1));method.instructions.insert(hook);count++;
      }
      if(mouse&&method.desc.equals("(FF)V")&&(method.name.equals("setAngles")||method.name.equals("func_70082_c")||m.equals("func_70082_c"))) {
        InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));
        hook.add(new VarInsnNode(FLOAD,1));hook.add(new VarInsnNode(FLOAD,2));
        hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"turn","(Lnet/minecraft/entity/Entity;FF)Z",false));
        hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;
      }
      if(living&&method.desc.equals("()V")&&(method.name.equals("onLivingUpdate")||method.name.equals("func_70636_d")||m.equals("func_70636_d"))) {
        InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));
        hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"living","(Lnet/minecraft/entity/EntityLivingBase;)Z",false));
        hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;
      }
      String packetNames=" processPlayer func_147347_a processPlayerBlockPlacement func_147346_a processUseEntity func_147340_a processPlayerDigging func_147345_a processClickWindow func_147351_a processCreativeInventoryAction func_147344_a processEnchantItem func_147338_a processHeldItemChange func_147355_a processEntityAction func_147357_a ";
      if(player&&(packetNames.contains(" "+method.name+" ")||packetNames.contains(" "+m+" "))) {
        FieldNode field=null;for(FieldNode f:node.fields)if(f.name.equals("playerEntity")||f.name.equals("field_147369_b")||FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(node.name,f.name,f.desc).equals("field_147369_b"))field=f;
        if(field!=null){InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,node.name,field.name,field.desc));hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"isControlling","(Lnet/minecraft/entity/Entity;)Z",false));hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;}
        String riderHook=method.name.equals("processPlayer")||method.name.equals("func_147347_a")||m.equals("func_147347_a")?"riderMovement":method.name.equals("processEntityAction")||method.name.equals("func_147357_a")||m.equals("func_147357_a")?"riderAction":null;
        if(field!=null&&riderHook!=null){InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,node.name,field.name,field.desc));hook.add(new VarInsnNode(ALOAD,1));String packet=riderHook.equals("riderMovement")?"C03PacketPlayer":"C0BPacketEntityAction";hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,riderHook,"(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/network/play/client/"+packet+";)Z",false));hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;}
      }
      boolean viewMethod=stream&&(method.name.equals("updatePlayerPertinentChunks")||method.name.equals("func_72685_d")||m.equals("func_72685_d")
          ||method.name.equals("filterChunkLoadQueue")||method.name.equals("func_72691_b")||m.equals("func_72691_b")
          ||method.name.equals("tryStartWachingThis")||method.name.equals("func_73117_b")||m.equals("func_73117_b"));
      boolean near=broadcast&&(method.name.equals("sendToAllNearExcept")||method.name.equals("func_148543_a")||m.equals("func_148543_a"));
      boolean active=activeWorld&&(method.name.equals("setActivePlayerChunksAndCheckLight")||method.name.equals("func_72903_x")||m.equals("func_72903_x"));
      boolean order=chunkOrder&&method.name.equals("<init>");
      if(viewMethod||near||active||order)for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof FieldInsnNode&&ins.getOpcode()==GETFIELD) {
        FieldInsnNode f=(FieldInsnNode)ins;
        // Only the method's player argument, never the tracked entity's own coordinates.
        AbstractInsnNode prev=ins.getPrevious();while(prev instanceof LabelNode||prev instanceof LineNumberNode||prev instanceof FrameNode)prev=prev.getPrevious();
        if(!near&&!active&&(!(prev instanceof VarInsnNode)||prev.getOpcode()!=ALOAD||((VarInsnNode)prev).var!=1))continue;
        String field=FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(f.owner,f.name,f.desc);
        String axis=f.name.equals("posX")||f.name.equals("field_70165_t")||field.equals("field_70165_t")?"viewX":
            f.name.equals("posZ")||f.name.equals("field_70161_v")||field.equals("field_70161_v")?"viewZ":
            (near||active)&&(f.name.equals("posY")||f.name.equals("field_70163_u")||field.equals("field_70163_u"))?"viewY":null;
        if(axis!=null){method.instructions.set(ins,new MethodInsnNode(INVOKESTATIC,HOOK,axis,"(Lnet/minecraft/entity/player/EntityPlayer;)D",false));count++;}
      }
    }
    if(count==0)return bytes;ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
  private static void guard(MethodNode method,int argument,String name,String desc,boolean falseReturn) {
    InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,argument));hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,name,desc,false));hook.add(new JumpInsnNode(IFEQ,normal));if(falseReturn)hook.add(new InsnNode(ICONST_0));hook.add(new InsnNode(falseReturn?IRETURN:RETURN));hook.add(normal);method.instructions.insert(hook);
  }
}
