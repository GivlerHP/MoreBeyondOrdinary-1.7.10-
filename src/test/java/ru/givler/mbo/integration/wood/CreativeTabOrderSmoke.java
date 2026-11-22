package ru.givler.mbo.integration.wood;

import ru.givler.mbo.registry.CreativeTabOrder;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.creativetab.CreativeTabs;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import ru.givler.mbo.core.CreativeTabOrderTransformer;

public final class CreativeTabOrderSmoke {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Item wood = new Item(), fence = new Item(), door = new Item(), unrelated = new Item();
        CreativeTabs tab = CreativeTabs.tabBlock;
        CreativeTabOrder.register(tab, new ItemStack(wood, 1, 0), new ItemStack(door), new ItemStack(fence, 1, 0));
        CreativeTabOrder.register(tab, new ItemStack(wood, 1, 1), new ItemStack(fence, 1, 1));
        List<ItemStack> items = new ArrayList<ItemStack>(Arrays.asList(new ItemStack(unrelated),
                new ItemStack(wood, 1, 0), new ItemStack(wood, 1, 1), new ItemStack(fence, 1, 1),
                new ItemStack(door), new ItemStack(fence, 1, 0)));
        CreativeTabOrder.arrange(tab, items);
        check(items.size() == 6 && items.get(0).getItem() == unrelated, "Unrelated entries retained");
        check(items.get(2).getItem() == door && items.get(3).getItem() == fence, "Family after source wood");
        check(items.get(4).getItem() == wood && items.get(5).getItemDamage() == 1, "Shared fence metadata retained");
        CreativeTabOrder.arrange(tab, items);
        check(items.size() == 6 && items.get(2).getItem() == door, "Repeated population must not duplicate");
        List<ItemStack> other = new ArrayList<ItemStack>(items);
        CreativeTabOrder.arrange(CreativeTabs.tabTools, other);
        check(other.equals(items), "Other tabs untouched");
        for (String methodName : new String[]{"displayAllReleventItems", "func_78018_a"}) {
            ClassWriter writer = new ClassWriter(0);
            writer.visit(52, Opcodes.ACC_PUBLIC, "net/minecraft/creativetab/CreativeTabs", null, "java/lang/Object", null);
            MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, methodName, "(Ljava/util/List;)V", null, null);
            method.visitCode(); method.visitInsn(Opcodes.RETURN); method.visitMaxs(0, 2); method.visitEnd(); writer.visitEnd();
            byte[] result = new CreativeTabOrderTransformer().transform(null, "net.minecraft.creativetab.CreativeTabs", writer.toByteArray());
            ClassNode node = new ClassNode(); new ClassReader(result).accept(node, 0);
            int hooks = 0;
            for (AbstractInsnNode instruction : node.methods.get(0).instructions.toArray())
                if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).name.equals("arrange")) hooks++;
            check(hooks == 1, "Exactly one hook in " + methodName);
        }
        System.out.println("PASS explicit family positions, metadata, no duplicates, other tabs, MCP/SRG hooks");
    }
}
