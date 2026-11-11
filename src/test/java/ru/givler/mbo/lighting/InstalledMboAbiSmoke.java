package ru.givler.mbo.lighting;
import java.net.URLClassLoader;
import java.io.File;
import net.minecraft.block.material.Material;
import ru.givler.mbo.block.BlockDecorModel;

/** Load the actual externally compiled telescope against the public MBO block superclass. */
public final class InstalledMboAbiSmoke {
  public static void main(String[] args)throws Exception {
    try(URLClassLoader loader=new URLClassLoader(new java.net.URL[]{new File(args[0]).toURI().toURL()},InstalledMboAbiSmoke.class.getClassLoader())) {
      Class<?> telescope=Class.forName("ru.givler.seasonalexpansion.block.ModelTelescope",false,loader);
      if(telescope.getClassLoader()!=loader)throw new AssertionError("Test must load the specified Seasonal Expansion JAR");
      if(telescope.getSuperclass()!=BlockDecorModel.class)throw new AssertionError("External model must extend MBO's actual model base");
      Class<?>[] signature={Material.class,String.class,String.class,String.class};
      telescope.getDeclaredConstructor(signature);
      BlockDecorModel.class.getConstructor(signature);
      if(BlockDecorModel.class.getMethod("withDynamicModelRender").getReturnType()!=BlockDecorModel.class)
        throw new AssertionError("Public fluent descriptor changed");
    }
    System.out.println("PASS installed Seasonal Expansion telescope class loading, MBO inheritance, constructor and fluent binary descriptors");
  }
}
