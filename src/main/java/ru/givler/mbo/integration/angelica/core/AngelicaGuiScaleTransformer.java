package ru.givler.mbo.integration.angelica.core;
import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Keep scissor coordinates in the same fractional scale as GUI projection. */
public final class AngelicaGuiScaleTransformer implements IClassTransformer,Opcodes {
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !target.equals("me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame"))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean changed=false;
    for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode) {
      MethodInsnNode call=(MethodInsnNode)i;
      String mapped=FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc);
      if(!call.desc.equals("()I") || !(mapped.equals("getScaleFactor") || mapped.equals("func_78325_e")))continue;
      AbstractInsnNode conversion=i.getNext();while(conversion!=null&&conversion.getOpcode()<0)conversion=conversion.getNext();
      if(conversion==null || (conversion.getOpcode()!=I2D && conversion.getOpcode()!=I2F))continue;
      int opcode=conversion.getOpcode();method.instructions.remove(conversion);
      call.setOpcode(INVOKESTATIC);call.owner="ru/givler/mbo/integration/angelica/client/gui/AngelicaGuiScale";
      call.name="factor";call.desc="(Lnet/minecraft/client/gui/ScaledResolution;)D";call.itf=false;
      if(opcode==I2F)method.instructions.insert(call,new InsnNode(D2F));changed=true;
    }
    if(!changed)return bytes;ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
  }
}
