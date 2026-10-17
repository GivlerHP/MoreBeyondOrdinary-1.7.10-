package ru.givler.mbo.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
public final class KeyBindingListTransformer implements IClassTransformer,Opcodes {
  public byte[] transform(String name,String target,byte[] bytes) {
    if(bytes==null || !target.equals("net.minecraft.client.gui.GuiKeyBindingList"))return bytes;
    ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int patched=0;
    String hook="ru/givler/mbo/client/gui/KeyBindingListSizing";
    for(MethodNode m:node.methods)if(m.name.equals("<init>")) {
      int bindings=-1;
      for(AbstractInsnNode i:m.instructions.toArray()) {
        if(i instanceof MethodInsnNode && ((MethodInsnNode)i).owner.equals(hook))return bytes;
        if(i instanceof TypeInsnNode) {
          TypeInsnNode t=(TypeInsnNode)i;
          String mapped=t.desc.startsWith("[")?FMLDeobfuscatingRemapper.INSTANCE.mapDesc(t.desc):t.desc;
          if(t.getOpcode()==CHECKCAST && mapped.equals("[Lnet/minecraft/client/settings/KeyBinding;")) {
            AbstractInsnNode next=i.getNext();while(next!=null && next.getOpcode()<0)next=next.getNext();
            if(next instanceof VarInsnNode && next.getOpcode()==ASTORE)bindings=((VarInsnNode)next).var;
          }
          String type=FMLDeobfuscatingRemapper.INSTANCE.map(t.desc);
          if(t.getOpcode()==ANEWARRAY && type.equals("net/minecraft/client/gui/GuiListExtended$IGuiListEntry")) {
            if(bindings<0)throw new IllegalStateException("Controls bindings clone anchor missing");
            InsnList code=new InsnList();code.add(new InsnNode(POP));code.add(new VarInsnNode(ALOAD,bindings));
            code.add(new MethodInsnNode(INVOKESTATIC,hook,"entries","([Lnet/minecraft/client/settings/KeyBinding;)I",false));
            m.instructions.insertBefore(i,code);patched++;
          }
        }
      }
    }
    if(patched!=1)throw new IllegalStateException("Controls entry allocation anchors: "+patched);
    ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
  }
}
