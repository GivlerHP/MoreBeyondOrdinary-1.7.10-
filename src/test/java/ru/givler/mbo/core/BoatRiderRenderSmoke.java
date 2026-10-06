package ru.givler.mbo.core;
import ru.givler.mbo.integration.thaumcraft.core.ThaumometerLensTransformer;

import java.lang.reflect.Field;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.client.model.ModelBiped;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoatSeat;
import ru.givler.mbo.client.render.BoatRiderRenderHooks;
import sun.misc.Unsafe;

public final class BoatRiderRenderSmoke {
  public static void main(String[] args) throws Exception {
    Field field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
    Unsafe unsafe = (Unsafe) field.get(null);
    EntityPlayerMP rider = (EntityPlayerMP) unsafe.allocateInstance(EntityPlayerMP.class);
    EntityMBOBoat boat = (EntityMBOBoat) unsafe.allocateInstance(EntityMBOBoat.class);
    boat.onGround = true;
    equal(0F, boat.getVisualYOffset());
    boat.onGround = false;
    equal(EntityMBOBoat.VISUAL_Y_OFFSET, boat.getVisualYOffset());
    boat.riddenByEntity = rider; rider.ridingEntity = boat;
    boat.prevRotationYaw = 170F; boat.rotationYaw = -170F;
    rider.rotationYaw = -60F;
    ru.givler.mbo.entity.boat.MBOBoatRiderView.clampView(rider);
    equal(-80F, rider.rotationYaw);
    rider.rotationYaw = 70F;
    ru.givler.mbo.entity.boat.MBOBoatRiderView.clampView(rider);
    equal(-260F, rider.rotationYaw);
    equal(180F, BoatRiderRenderHooks.bodyYaw(rider, 50F, 0.5F));
    ModelBiped model = new ModelBiped();
    model.bipedBody.rotateAngleY = 0.4F;
    model.bipedHead.rotateAngleY = 1F; model.bipedRightArm.rotateAngleY = 0.8F;
    model.bipedRightLeg.rotateAngleX = -1.25F;
    BoatRiderRenderHooks.pose(model, rider);
    equal(0F, model.bipedBody.rotateAngleY);
    equal(1F, model.bipedHead.rotateAngleY);
    equal(0.8F, model.bipedRightArm.rotateAngleY);
    equal(-1.25F, model.bipedRightLeg.rotateAngleX);
    rider.ridingEntity = (EntityMBOBoatSeat) unsafe.allocateInstance(EntityMBOBoatSeat.class);
    rider.rotationYaw = 70F;
    ru.givler.mbo.entity.boat.MBOBoatRiderView.clampView(rider);
    equal(70F, rider.rotationYaw);
    equal(50F, BoatRiderRenderHooks.bodyYaw(rider, 50F, 0.5F));
    model.bipedBody.rotateAngleY = 0.4F;
    BoatRiderRenderHooks.pose(model, rider);
    equal(0.4F, model.bipedBody.rotateAngleY);
    verify("net.minecraft.client.model.ModelBiped");
    verify("net.minecraft.entity.Entity");
    verify("net.minecraft.client.renderer.entity.RendererLivingEntity");
    verifyLens();
    System.out.println("Boat driver pose, passenger freedom and ASM checks passed.");
  }
  private static void verifyLens() throws Exception {
    String name = "thaumcraft.client.renderers.item.ItemThaumometerRenderer";
    ClassReader reader;
    try (InputStream stream = BoatRiderRenderSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
      reader = new ClassReader(stream);
    }
    ClassNode node = new ClassNode();
    new ClassReader(new ThaumometerLensTransformer().transform(name, name, reader.b)).accept(node, 0);
    int hooks = 0;
    for (MethodNode method : node.methods) {
      if (!method.name.startsWith("<")) new Analyzer(new BasicVerifier()).analyze(node.name, method);
      for (AbstractInsnNode instruction : method.instructions.toArray())
        if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).owner.endsWith("ThaumometerLens")) hooks++;
    }
    if (hooks != 1) throw new AssertionError("Thaumometer lens hook count=" + hooks);
    System.out.println("Thaumometer lens ASM check passed.");
  }
  private static void equal(float expected, float actual) {
    if (Math.abs(expected - actual) > 0.0001F) throw new AssertionError(expected + " != " + actual);
  }
  private static void verify(String name) throws Exception {
    byte[] source;
    try (InputStream stream = BoatRiderRenderSmoke.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
      ClassNode original = new ClassNode(); new ClassReader(stream).accept(original, 0);
      org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0); original.accept(writer); source = writer.toByteArray();
    }
    byte[] result = new BoatRiderRenderTransformer().transform(name, name, source);
    result = new mods.battlegear2.coremod.transformers.HorseRiderRenderTransformer().transform(name, name, result);
    ClassNode node = new ClassNode(); new ClassReader(result).accept(node, 0);
    for (MethodNode method : node.methods) if (!method.name.startsWith("<"))
      new Analyzer(new BasicVerifier()).analyze(node.name, method);
  }
}
