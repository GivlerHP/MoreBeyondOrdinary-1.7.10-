package ru.givler.mbo.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class DecorSleepTransformer implements IClassTransformer,Opcodes {
 public byte[] transform(String name,String transformedName,byte[] bytes) {
  if(bytes==null||!"net.minecraft.entity.player.EntityPlayer".equals(transformedName))return bytes;
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean sleep=false,wake=false;
  String player="net/minecraft/entity/player/EntityPlayer",hooks="ru/givler/mbo/core/DecorSleepHooks";
  for(MethodNode method:node.methods) {
   String mapped=cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
   if((method.name.equals("wakeUpPlayer")||method.name.equals("func_70999_a")||mapped.equals("func_70999_a"))&&method.desc.equals("(ZZZ)V")) {
    InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ILOAD,3));
    code.add(new MethodInsnNode(INVOKESTATIC,hooks,"setSpawnOnWake","(L"+player+";Z)Z",false));code.add(new VarInsnNode(ISTORE,3));method.instructions.insert(code);wake=true;
   }
   if((method.name.equals("sleepInBedAt")||method.name.equals("func_71018_a")||mapped.equals("func_71018_a"))&&method.desc.startsWith("(III)")) {
    int result=method.maxLocals++;
    for(AbstractInsnNode ins=method.instructions.getFirst();ins!=null;ins=ins.getNext())if(ins.getOpcode()==ARETURN) {
     InsnList code=new InsnList();code.add(new VarInsnNode(ASTORE,result));code.add(new VarInsnNode(ALOAD,0));
     code.add(new VarInsnNode(ILOAD,1));code.add(new VarInsnNode(ILOAD,2));code.add(new VarInsnNode(ILOAD,3));code.add(new VarInsnNode(ALOAD,result));
     code.add(new MethodInsnNode(INVOKESTATIC,hooks,"afterSleep","(L"+player+";IIIL"+player+"$EnumStatus;)L"+player+"$EnumStatus;",false));method.instructions.insertBefore(ins,code);
    }sleep=true;
   }
  }
  if(!sleep||!wake)throw new IllegalStateException("Decor sleep hooks missing");
  ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
 }
}
