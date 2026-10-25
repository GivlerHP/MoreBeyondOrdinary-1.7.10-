package ru.givler.mbo.lighting;
import java.lang.reflect.*;
import java.util.*;
import ru.givler.mbo.integration.angelica.client.gui.AngelicaColoredLightOptions;
import ru.givler.mbo.config.ColoredLightConfig;
public final class AngelicaOptionsSmoke { public static final class ScaleValue { public static float value(){return 1.35F;} }
  public static void main(String[] args)throws Exception {
    Field locale=net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");locale.setAccessible(true);
    locale.set(null,new net.minecraft.client.resources.Locale());
    if(args.length==0) {
      Class<?> config=Class.forName("com.github.basdxz.rightproperguiscale.config.RightProperGUIScaleConfig");
      config.getField("GUI_SCALE_MIN").setDouble(null,1D);
      config.getField("GUI_SCALE_MAX").setDouble(null,4D);
      config.getField("GUI_SCALE_STEP").setDouble(null,.05D);
      Method factory=ru.givler.mbo.integration.angelica.client.gui.AngelicaGuiScale.class.getDeclaredMethod("option",Class.class);factory.setAccessible(true);
      Object scaleOption=factory.invoke(null,ScaleValue.class);
      Object initial=scaleOption.getClass().getMethod("getValue").invoke(scaleOption);
      if(!Integer.valueOf(7).equals(initial))throw new AssertionError("Fractional 1.35 scale rounded to integer");
      Object scaleControl=scaleOption.getClass().getMethod("getControl").invoke(scaleOption);
      Field scaleFormatter=scaleControl.getClass().getDeclaredField("mode");scaleFormatter.setAccessible(true);
      Object label=Class.forName("me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter").getMethod("format",int.class).invoke(scaleFormatter.get(scaleControl),9);
      if(!"1.45x".equals(label))throw new AssertionError("Fractional scale label: "+label);
      scaleOption.getClass().getMethod("setValue",Object.class).invoke(scaleOption,9);
      scaleOption.getClass().getMethod("reset").invoke(scaleOption);
      if(!initial.equals(scaleOption.getClass().getMethod("getValue").invoke(scaleOption)))throw new AssertionError("Fractional undo");
      System.out.println("RightProperGUIScale: real config API, 0.05 step, 1.35 initial value, 1.45 label and Undo passed");
    }
    Object page=AngelicaColoredLightOptions.createPage();
    if(!"RGB-MBO".equals(page.getClass().getMethod("getName").invoke(page)))throw new AssertionError("Page title");
    List<?> options=(List<?>)page.getClass().getMethod("getOptions").invoke(page);
    if(options.size()!=7)throw new AssertionError("Missing RGB options");
    Object particleLight=options.get(4);
    if(!"mbo.light.particles".equals(particleLight.getClass().getMethod("getName").invoke(particleLight)))throw new AssertionError("Particle light is not in RGB tab");
    String root="me.jellysquid.mods.sodium.client.gui.options.";
    Class<?> optionType=Class.forName(root+"Option");
    Object quantityGroup=Class.forName(root+"OptionGroup").getMethod("createBuilder").invoke(null);
    // Reuse a native option with the vanilla quantity label to exercise page reconstruction.
    Field nameField=particleLight.getClass().getDeclaredField("name");nameField.setAccessible(true);
    nameField.set(particleLight,"options.particles");
    quantityGroup.getClass().getMethod("add",optionType).invoke(quantityGroup,particleLight);
    Object builtGroup=quantityGroup.getClass().getMethod("build").invoke(quantityGroup);
    List<Object> pages=new ArrayList<Object>();
    pages.add(Class.forName(root+"OptionPage").getConstructor(String.class,com.google.common.collect.ImmutableList.class)
        .newInstance("General",com.google.common.collect.ImmutableList.of(builtGroup)));
    AngelicaColoredLightOptions.attachCubicParticles(pages);
    List<?> general=(List<?>)pages.get(0).getClass().getMethod("getOptions").invoke(pages.get(0));
    if(general.size()!=2||!"mbo.light.cubicParticles".equals(general.get(1).getClass().getMethod("getName").invoke(general.get(1))))throw new AssertionError("3D toggle must follow particle quantity");
    AngelicaColoredLightOptions.attachCubicParticles(pages);
    if(((List<?>)pages.get(0).getClass().getMethod("getOptions").invoke(pages.get(0))).size()!=2)throw new AssertionError("Duplicate 3D toggle");
    nameField.set(particleLight,"mbo.light.particles");
    Object enabled=options.get(0);Class<?> type=enabled.getClass();
    boolean initial=ColoredLightConfig.enabled;
    type.getMethod("setValue",Object.class).invoke(enabled,!initial);
    if(!Boolean.TRUE.equals(type.getMethod("hasChanged").invoke(enabled)) || ColoredLightConfig.enabled!=initial)throw new AssertionError("Pending setting applied before Apply");
    type.getMethod("reset").invoke(enabled);
    if(!Boolean.FALSE.equals(type.getMethod("hasChanged").invoke(enabled)))throw new AssertionError("Undo failed");
    for(Object option:options)if(option.getClass().getMethod("getControl").invoke(option)==null)throw new AssertionError("Native control missing");
    Object speed=options.get(6),control=speed.getClass().getMethod("getControl").invoke(speed);
    Field formatter=control.getClass().getDeclaredField("mode");formatter.setAccessible(true);
    Class<?> formatterType=Class.forName("me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter");
    for(int i=0;i<3;i++)if(formatterType.getMethod("format",int.class).invoke(formatter.get(control),i)==null)throw new AssertionError("Speed label missing");
    System.out.println("Actual Angelica API: RGB-MBO native page, seven controls, pending Apply/Undo semantics and speed labels passed");
  }
}
