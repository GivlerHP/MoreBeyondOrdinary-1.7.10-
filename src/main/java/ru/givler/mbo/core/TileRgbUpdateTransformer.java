package ru.givler.mbo.core;
import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** All mods' tile packets invalidate just the updated RGB cell. */
public final class TileRgbUpdateTransformer implements IClassTransformer,Opcodes {
  private static final String HOOK="ru/givler/mbo/client/lighting/NativeRgbSources";
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !target.equals("net.minecraft.client.network.NetHandlerPlayClient"))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int hooks=0;
    for(MethodNode m:node.methods) {
      if(!FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(m.desc).equals("(Lnet/minecraft/network/play/server/S35PacketUpdateTileEntity;)V"))continue;
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
      for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode) {
        MethodInsnNode call=(MethodInsnNode)i;
        if(call.name.equals("onDataPacket")) {
          InsnList code=new InsnList();code.add(new VarInsnNode(ALOAD,1));code.add(new MethodInsnNode(INVOKESTATIC,HOOK,"updated","(Lnet/minecraft/network/play/server/S35PacketUpdateTileEntity;)V",false));m.instructions.insert(i,code);hooks++;
        }
      }
    }
    if(hooks!=1)throw new IllegalStateException("Tile RGB update anchors: "+hooks);
    ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();
  }
}
