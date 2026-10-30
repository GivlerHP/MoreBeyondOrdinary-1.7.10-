package ru.givler.mbo.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/** Stop species AI before it can feed, attack or alter terrain; reuse native camera streaming. */
public final class PossessionTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/magic/possession/PossessionManager";
  public byte[] transform(String name,String mapped,byte[] bytes) {
    if(bytes==null)return null;
    boolean living=mapped.equals("net.minecraft.entity.EntityLivingBase")||mapped.startsWith("net.minecraft.entity.passive.")
        ||mapped.startsWith("ru.givler.mbo.entity.fauna.Entity");
    boolean player=mapped.equals("net.minecraft.network.NetHandlerPlayServer");
    boolean mouse=mapped.equals("net.minecraft.entity.Entity");
    boolean stream=mapped.equals("net.minecraft.server.management.PlayerManager")||mapped.equals("net.minecraft.entity.EntityTrackerEntry");
    if(!living&&!player&&!stream&&!mouse)return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
    for(MethodNode method:node.methods) {
      boolean patched=false;for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).owner.equals(HOOK))patched=true;
      if(patched)continue;
      String m=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
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
      String packetNames=" processPlayer func_147347_a processPlayerBlockPlacement func_147346_a processUseEntity func_147340_a processPlayerDigging func_147345_a processClickWindow func_147351_a processCreativeInventoryAction func_147344_a processEnchantItem func_147338_a processHeldItemChange func_147355_a ";
      if(player&&(packetNames.contains(" "+method.name+" ")||packetNames.contains(" "+m+" "))) {
        FieldNode field=null;for(FieldNode f:node.fields)if(f.name.equals("playerEntity")||f.name.equals("field_147369_b")||FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(node.name,f.name,f.desc).equals("field_147369_b"))field=f;
        if(field!=null){InsnList hook=new InsnList();LabelNode normal=new LabelNode();hook.add(new VarInsnNode(ALOAD,0));hook.add(new FieldInsnNode(GETFIELD,node.name,field.name,field.desc));hook.add(new MethodInsnNode(INVOKESTATIC,HOOK,"isControlling","(Lnet/minecraft/entity/Entity;)Z",false));hook.add(new JumpInsnNode(IFEQ,normal));hook.add(new InsnNode(RETURN));hook.add(normal);method.instructions.insert(hook);count++;}
      }
      boolean viewMethod=stream&&(method.name.equals("updatePlayerPertinentChunks")||method.name.equals("func_72685_d")||m.equals("func_72685_d")
          ||method.name.equals("filterChunkLoadQueue")||method.name.equals("func_72691_b")||m.equals("func_72691_b")
          ||method.name.equals("tryStartWachingThis")||method.name.equals("func_73117_b")||m.equals("func_73117_b"));
      if(viewMethod)for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof FieldInsnNode&&ins.getOpcode()==GETFIELD) {
        FieldInsnNode f=(FieldInsnNode)ins;
        // Only the method's player argument, never the tracked entity's own coordinates.
        AbstractInsnNode prev=ins.getPrevious();while(prev instanceof LabelNode||prev instanceof LineNumberNode||prev instanceof FrameNode)prev=prev.getPrevious();
        if(!(prev instanceof VarInsnNode)||prev.getOpcode()!=ALOAD||((VarInsnNode)prev).var!=1)continue;
        String field=FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(f.owner,f.name,f.desc);
        String axis=f.name.equals("posX")||f.name.equals("field_70165_t")||field.equals("field_70165_t")?"viewX":
            f.name.equals("posZ")||f.name.equals("field_70161_v")||field.equals("field_70161_v")?"viewZ":null;
        if(axis!=null){method.instructions.set(ins,new MethodInsnNode(INVOKESTATIC,HOOK,axis,"(Lnet/minecraft/entity/player/EntityPlayer;)D",false));count++;}
      }
    }
    if(count==0)return bytes;ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
  }
}
