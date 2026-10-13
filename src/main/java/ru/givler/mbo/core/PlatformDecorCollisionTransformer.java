package ru.givler.mbo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Feed moving decor into vanilla swept collision rather than pushing after a wall was crossed. */
public final class PlatformDecorCollisionTransformer implements IClassTransformer,Opcodes {
 public byte[] transform(String name,String transformedName,byte[] bytes) {
  if(bytes==null||!"net.minecraft.world.World".equals(transformedName))return bytes;
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int patched=0;
  for(MethodNode method:node.methods) {
   String mapped=cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc);
   if(!(method.name.equals("getCollidingBoundingBoxes")||method.name.equals("func_72945_a")||mapped.equals("func_72945_a")))continue;
   if(!cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))continue;
   for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==ARETURN) {
    InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new VarInsnNode(ALOAD,1));code.add(new VarInsnNode(ALOAD,2));
    code.add(new MethodInsnNode(INVOKESTATIC,"ru/givler/mbo/movingplatform/MovingPlatformTickHandler","addDecorCollisions",
        "(Ljava/util/List;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;",false));
    method.instructions.insertBefore(ins,code);patched++;
   }
  }
  if(patched==0)throw new IllegalStateException("Moving decor world collision hook missing");
  ClassWriter writer=SafeClassWriter.create();node.accept(writer);return writer.toByteArray();
 }
}
