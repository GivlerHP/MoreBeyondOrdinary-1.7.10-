package ru.givler.mbo.core;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Run the registered coremod chain on Forge-patched classes with real notch names/descriptors. */
public final class ProductionTransformersSmoke {
  static final class CachedMaps extends HashMap<String,Map<String,String>> {
    @Override public boolean containsKey(Object owner) { return true; }
    @Override public Map<String,String> get(Object owner) { Map<String,String> result = super.get(owner); return result == null ? Collections.emptyMap() : result; }
  }
  static final Map<String, ClassNode> hierarchy = new HashMap<>();
  static Mapping productionNames;
  static Map<String,byte[]> optifineInputs(Set<String> targets, Mapping toNotch, String[] args) throws Exception {
    Map<String,byte[]> inputs = new HashMap<>(); if (args.length < 3) return inputs;
    Class<?> patcher = Class.forName("optifine.Patcher"), providerType = Class.forName("optifine.IResourceProvider");
    try (java.util.zip.ZipFile of = new java.util.zip.ZipFile(args[1]); java.util.zip.ZipFile vanilla = new java.util.zip.ZipFile(args[2])) {
      Object provider = java.lang.reflect.Proxy.newProxyInstance(providerType.getClassLoader(),new Class[]{providerType},(p,m,a) -> {
        String path = (String) a[0]; if (path.startsWith("/")) path = path.substring(1);
        java.util.zip.ZipEntry entry = vanilla.getEntry(path); return entry == null ? null : vanilla.getInputStream(entry);
      });
      Map<?,?> config = (Map<?,?>) patcher.getMethod("getConfigurationMap",java.util.zip.ZipFile.class).invoke(null,of);
      Object patterns = patcher.getMethod("getConfigurationPatterns",Map.class).invoke(null,config);
      java.lang.reflect.Method apply = patcher.getMethod("applyPatch",String.class,byte[].class,patterns.getClass(),Map.class,providerType);
      for (String target : targets) {
        if (!target.startsWith("net.minecraft.")) continue;
        String path = toNotch.map(target.replace('.','/')) + ".class";
        java.util.zip.ZipEntry delta = of.getEntry("patch/" + path + ".xdelta"), direct = of.getEntry(path);
        byte[] bytes = null;
        if (delta != null) bytes = (byte[]) apply.invoke(null,path,readStream(of.getInputStream(delta)),patterns,config,provider);
        else if (direct != null) bytes = readStream(of.getInputStream(direct));
        if (bytes != null) { inputs.put(target,bytes); Path output = Paths.get("build/reports/optifine-input",path); Files.createDirectories(output.getParent()); Files.write(output,bytes); }
      }
    }
    return inputs;
  }
  static byte[] readStream(InputStream in) throws IOException {
    try (InputStream stream = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192]; int n; while ((n = stream.read(buffer)) >= 0) out.write(buffer,0,n); return out.toByteArray();
    }
  }
  static class Mapping extends Remapper {
    final Map<String,String> classes = new HashMap<>(), methods = new HashMap<>(), fields = new HashMap<>();
    Mapping(Path file) throws IOException {
      for (String line : Files.readAllLines(file)) {
        String[] p = line.split(" ");
        if (p[0].equals("CL:")) classes.put(p[1], p[2]);
        if (p[0].equals("MD:")) methods.put(p[1] + p[2], tail(p[3]));
        if (p[0].equals("FD:")) fields.put(p[1], tail(p[2]));
      }
    }
    public String map(String name) { return classes.containsKey(name) ? classes.get(name) : name; }
    public String mapMethodName(String owner,String name,String desc) { return inherited(owner, name, desc, methods, new HashSet<>()); }
    public String mapFieldName(String owner,String name,String desc) { return inherited(owner, name, "", fields, new HashSet<>()); }
    String inherited(String owner,String name,String desc,Map<String,String> entries,Set<String> visited) {
      String found = entries.get(owner + "/" + name + desc); if (found != null) return found;
      if (!visited.add(owner)) return name;
      ClassNode node = hierarchy.get(owner);
      if (node == null && owner.startsWith("net/")) try { byte[] bytes = resource(owner); if (bytes != null) { node = read(bytes); hierarchy.put(owner,node); } } catch (IOException ignored) {}
      if (node == null && productionNames != null && classes.containsKey(owner) && classes.get(owner).startsWith("net/")) try {
        byte[] bytes = resource(classes.get(owner));
        if (bytes != null) { hierarchy.put(classes.get(owner),read(bytes)); node = read(remap(bytes,productionNames)); hierarchy.put(owner,node); }
      } catch (IOException ignored) {}
      if (node != null) {
        List<String> parents = new ArrayList<>(node.interfaces); if (node.superName != null) parents.add(node.superName);
        for (String parent : parents) { found = inherited(parent,name,desc,entries,visited); if (!found.equals(name)) return found; }
      }
      return name;
    }
  }
  static String tail(String name) { return name.substring(name.lastIndexOf('/') + 1); }
  static ClassNode read(byte[] bytes) { ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node,0); return node; }
  static byte[] resource(String name) throws IOException {
    try (InputStream in = ProductionTransformersSmoke.class.getClassLoader().getResourceAsStream(name + ".class")) {
      if (in == null) return null; ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b = new byte[8192]; int n;
      while ((n = in.read(b)) >= 0) out.write(b,0,n); return out.toByteArray();
    }
  }
  static byte[] remap(byte[] bytes, Remapper mapping) {
    ClassWriter writer = new ClassWriter(0); new ClassReader(bytes).accept(new RemappingClassAdapter(writer,mapping),ClassReader.EXPAND_FRAMES); return writer.toByteArray();
  }
  static Map<String,Integer> hooks(byte[] bytes) {
    Map<String,Integer> result = new TreeMap<>();
    for (MethodNode method : read(bytes).methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
      if (insn instanceof MethodInsnNode) {
        MethodInsnNode call = (MethodInsnNode) insn;
        if (call.owner.startsWith("ru/givler/mbo/")) { String key = call.owner + "/" + call.name + call.desc; result.put(key,result.getOrDefault(key,0)+1); }
      }
      if (insn instanceof TypeInsnNode && ((TypeInsnNode) insn).desc.startsWith("ru/givler/mbo/")) {
        String key = "type:" + ((TypeInsnNode) insn).desc; result.put(key,result.getOrDefault(key,0)+1);
      }
    }
    return result;
  }
  static void verify(byte[] bytes) throws AnalyzerException {
    ClassNode node = read(bytes);
    for (MethodNode method : node.methods) if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
      new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name, method);
  }
  static void verifyHookLinks(byte[] bytes, Mapping toMcp) throws Exception {
    for (MethodNode method : read(bytes).methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
      if (!(instruction instanceof MethodInsnNode)) continue;
      MethodInsnNode call = (MethodInsnNode) instruction;
      if (!call.owner.startsWith("ru/givler/mbo/")) continue;
      String desc = toMcp.mapMethodDesc(call.desc);
      byte[] ownerBytes = resource(call.owner); if (ownerBytes == null) throw new AssertionError("Missing hook class: " + call.owner);
      boolean found = false;
      for (MethodNode candidate : read(ownerBytes).methods) if (candidate.name.equals(call.name) && candidate.desc.equals(desc)) {
        if ((call.getOpcode() == Opcodes.INVOKESTATIC) != ((candidate.access & Opcodes.ACC_STATIC) != 0)) throw new AssertionError("Hook invocation kind: " + call.owner + "/" + call.name);
        found = true; break;
      }
      if (!found) throw new AssertionError("Missing hook method: " + call.owner + "/" + call.name + desc);
    }
  }
  static void cacheOwner(String owner, Mapping toNotch, Mapping toMcp, Mapping toSrg,
      Map<String,Map<String,String>> methods, Map<String,Map<String,String>> fields, Set<String> cached) throws IOException {
    if (!cached.add(owner)) return;
    Map<String,String> ownMethods = new HashMap<>(), ownFields = new HashMap<>();
    methods.put(owner,ownMethods); fields.put(owner,ownFields);
    byte[] bytes = resource(toMcp.map(owner)); if (bytes == null) return;
    ClassNode node = read(bytes);
    List<String> parents = new ArrayList<>(node.interfaces); if (node.superName != null) parents.add(node.superName);
    for (String parent : parents) {
      String raw = toNotch.map(parent); cacheOwner(raw,toNotch,toMcp,toSrg,methods,fields,cached);
      ownMethods.putAll(methods.get(raw)); ownFields.putAll(fields.get(raw));
    }
    for (MethodNode method : node.methods) {
      String raw = toNotch.mapMethodName(node.name,method.name,method.desc), desc = toNotch.mapMethodDesc(method.desc);
      String srg = toSrg.methods.get(owner + "/" + raw + desc);
      if (srg != null) ownMethods.put(raw + desc,srg);
    }
    for (FieldNode field : node.fields) {
      String raw = toNotch.mapFieldName(node.name,field.name,field.desc), desc = toNotch.mapDesc(field.desc);
      String srg = toSrg.fields.get(owner + "/" + raw);
      if (srg != null) ownFields.put(raw + ":" + desc,srg);
    }
  }
  static Map<String,Integer> delta(byte[] before,byte[] after) {
    Map<String,Integer> a = hooks(before), b = hooks(after);
    for (Map.Entry<String,Integer> e : a.entrySet()) b.put(e.getKey(),b.getOrDefault(e.getKey(),0)-e.getValue());
    b.values().removeIf(v -> v == 0); return b;
  }
  public static void main(String[] args) throws Exception {
    byte[] container=resource("net/minecraft/client/gui/inventory/GuiContainer");
    if(new ItemTooltipContextTransformer().transform("bex","net.minecraft.client.gui.inventory.GuiContainer",container)!=container)
      throw new AssertionError("Item tooltip context changed GuiContainer and could break NEI instruction matching");
    System.out.println("NEI compatibility: GuiContainer tooltip instruction sequence remains untouched");
    Path mappingDir = Paths.get(args[0]);
    Mapping srgToMcp=new Mapping(mappingDir.resolve("srg-mcp.srg"));
    for(String renderer:new String[]{"client/render/PlatformVertexLighting","client/render/decormodels/ChunkDecorRenderer","client/render/decormodels/StaticDecorRenderCache"}) {
      ClassNode reflected=new ClassNode();new ClassReader(resource("ru/givler/mbo/"+renderer)).accept(reflected,0);
      boolean checked=false;
      for(MethodNode method:reflected.methods)if(method.name.equals("<clinit>"))for(AbstractInsnNode i:method.instructions.toArray()) {
        if(!(i instanceof LdcInsnNode) || !"rawBufferIndex".equals(((LdcInsnNode)i).cst))continue;
        for(AbstractInsnNode next=i.getNext();next!=null;next=next.getNext())if(next instanceof LdcInsnNode && ((LdcInsnNode)next).cst instanceof String) {
          String alias=(String)((LdcInsnNode)next).cst;
          if(!"rawBufferIndex".equals(srgToMcp.fields.get("net/minecraft/client/renderer/Tessellator/"+alias)))
            throw new AssertionError(renderer+" rawBufferIndex alias resolves to "+srgToMcp.fields.get("net/minecraft/client/renderer/Tessellator/"+alias));
          checked=true;break;
        }
      }
      if(!checked)throw new AssertionError("Missing production buffer-index alias in "+renderer);
    }
    System.out.println("Production reflection: platform and both decor renderers resolve rawBufferIndex against actual SRG mappings");
    Mapping toNotch = new Mapping(mappingDir.resolve("mcp-notch.srg"));
    productionNames = toNotch;
    Mapping toMcp = new Mapping(mappingDir.resolve("notch-mcp.srg"));
    Mapping toSrg = new Mapping(mappingDir.resolve("notch-srg.srg"));
    List<IClassTransformer> chain = new ArrayList<>(); Set<String> targets = new TreeSet<>();
    Pattern quoted = Pattern.compile("\"([A-Za-z0-9_.$/]+)\"");
    for (String transformer : new MBOCorePlugin().getASMTransformerClass()) {
      chain.add((IClassTransformer) Class.forName(transformer).newInstance());
      String source = new String(Files.readAllBytes(Paths.get("src/main/java/" + transformer.replace('.','/') + ".java")),StandardCharsets.UTF_8);
      Matcher matcher = quoted.matcher(source);
      while (matcher.find()) {
        String name = matcher.group(1).replace('/','.');
        if ((name.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+") || name.equals("DynamicLights")) && !name.startsWith("ru.givler.mbo.") && !name.startsWith("java.") && !name.equals("fml.deobfuscatedEnvironment")) targets.add(name);
      }
    }
    Object remapper = FMLDeobfuscatingRemapper.INSTANCE;
    Map<String,byte[]> ofInputs = optifineInputs(targets,toNotch,args);
    for (String target : ofInputs.keySet()) targets.add("OptiFine:" + target);
    Field classField = remapper.getClass().getDeclaredField("classNameBiMap"); classField.setAccessible(true);
    Field methodField = remapper.getClass().getDeclaredField("methodNameMaps"); methodField.setAccessible(true);
    Field fieldField = remapper.getClass().getDeclaredField("fieldNameMaps"); fieldField.setAccessible(true);
    Object previousClasses = classField.get(remapper), previousMethods = methodField.get(remapper), previousFields = fieldField.get(remapper);
    com.google.common.collect.BiMap<String,String> classes = com.google.common.collect.HashBiMap.create(toSrg.classes);
    Map<String,Map<String,String>> methods = new CachedMaps(), fields = new CachedMaps();
    for (String owner : classes.keySet()) { methods.put(owner,new HashMap<>()); fields.put(owner,new HashMap<>()); }
    for (Map.Entry<String,String> entry : toSrg.methods.entrySet()) {
      int slash = entry.getKey().lastIndexOf('/',entry.getKey().indexOf('('));
      methods.computeIfAbsent(entry.getKey().substring(0,slash), k -> new HashMap<>()).put(entry.getKey().substring(slash+1),entry.getValue());
    }
    for (Map.Entry<String,String> entry : toSrg.fields.entrySet()) {
      int slash = entry.getKey().lastIndexOf('/'); fields.computeIfAbsent(entry.getKey().substring(0,slash), k -> new HashMap<>()).put(entry.getKey().substring(slash+1),entry.getValue());
    }
    List<String> report = new ArrayList<>(), failures = new ArrayList<>(); Set<String> exercised = new TreeSet<>(); int available = 0;
    Set<String> cached = new HashSet<>();
    PrintStream stdout = System.out, stderr = System.err;
    Map<String,Object> previousBlackboard = net.minecraft.launchwrapper.Launch.blackboard;
    net.minecraft.launchwrapper.Launch.blackboard = new HashMap<>();
    try {
      for (String caseName : targets) {
        boolean optifine = caseName.startsWith("OptiFine:"); String target = optifine ? caseName.substring(9) : caseName;
        byte[] input = optifine ? remap(ofInputs.get(target),toMcp) : resource(target.replace('.','/')); if (input == null) { report.add("UNAVAILABLE " + target); continue; }
        if (target.equals("DynamicLights") || target.startsWith("shadersmod.")) input = remap(input,toMcp);
        available++;
        // Non-Minecraft classes also contain obfuscated vanilla references in production.
        byte[] production = remap(input,toNotch), development = input;
        methods.putIfAbsent(target.replace('.','/'),new HashMap<>()); fields.putIfAbsent(target.replace('.','/'),new HashMap<>());
        ByteArrayOutputStream messages = new ByteArrayOutputStream(); System.setOut(new PrintStream(messages)); System.setErr(new PrintStream(messages));
        try {
          for (IClassTransformer transformer : chain) {
            String label = transformer.getClass().getSimpleName();
            classField.set(remapper,previousClasses); methodField.set(remapper,previousMethods); fieldField.set(remapper,previousFields);
            net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",true);
            byte[] dev = transformer.transform(target,target,development);
            ClassNode rawNode = read(production); cacheOwner(rawNode.name,toNotch,toMcp,toSrg,methods,fields,cached);
            for (MethodNode method : rawNode.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
              String owner = insn instanceof MethodInsnNode ? ((MethodInsnNode) insn).owner : insn instanceof FieldInsnNode ? ((FieldInsnNode) insn).owner : null;
              if (owner != null) cacheOwner(owner,toNotch,toMcp,toSrg,methods,fields,cached);
            }
            classField.set(remapper,classes); methodField.set(remapper,methods); fieldField.set(remapper,fields);
            net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",false);
            byte[] prod = transformer.transform(toNotch.map(target.replace('.','/')).replace('/','.'),target,production);
            boolean devChanged = !Arrays.equals(development,dev), prodChanged = !Arrays.equals(production,prod);
            if (devChanged || prodChanged) {
              exercised.add(label); report.add(label + " " + caseName + " dev=" + devChanged + " production=" + prodChanged);
              if (devChanged != prodChanged || !delta(development,dev).equals(delta(remap(production,toMcp),remap(prod,toMcp)))) failures.add(label + " " + target + " dev/prod patch mismatch: " + delta(development,dev) + " / " + delta(remap(production,toMcp),remap(prod,toMcp)));
              verify(prod);
              verifyHookLinks(prod,toMcp);
            }
            development = dev; production = prod;
          }
        } catch (Throwable error) { failures.add(caseName + ": " + error); error.printStackTrace(new PrintStream(messages)); }
        finally { System.setOut(stdout); System.setErr(stderr); }
        if (messages.size() > 0) report.add(messages.toString("UTF-8"));
      }
    } finally { System.setOut(stdout); System.setErr(stderr); classField.set(remapper,previousClasses); methodField.set(remapper,previousMethods); fieldField.set(remapper,previousFields); net.minecraft.launchwrapper.Launch.blackboard = previousBlackboard; }
    for (IClassTransformer transformer : chain) if (!exercised.contains(transformer.getClass().getSimpleName())) report.add("NOT_EXERCISED " + transformer.getClass().getSimpleName());
    report.addAll(failures); Files.createDirectories(Paths.get("build/reports")); Files.write(Paths.get("build/reports/production-transformers.txt"),report,StandardCharsets.UTF_8);
    for (String failure : failures) stdout.println(failure);
    stdout.println("Registered=" + chain.size() + ", available classes=" + available + ", exercised=" + exercised.size() + ", failures=" + failures.size());
    verifyWithoutMf2();
    if (!failures.isEmpty()) throw new AssertionError("Production transformer audit failed; see build/reports/production-transformers.txt");
  }
  static void verifyWithoutMf2() throws Exception {
    final String helper = "ru.givler.mbo.client.lighting.ColoredLightAuto";
    ClassLoader isolated = new ClassLoader(ProductionTransformersSmoke.class.getClassLoader()) {
      @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("minefantasy.") || name.startsWith("mods.battlegear2.")) throw new ClassNotFoundException(name);
        if (!name.equals(helper)) return super.loadClass(name,resolve);
        Class<?> type = findLoadedClass(name);
        if (type == null) try { byte[] bytes = resource(name.replace('.','/')); type = defineClass(name,bytes,0,bytes.length); }
        catch (IOException error) { throw new ClassNotFoundException(name,error); }
        if (resolve) resolveClass(type); return type;
      }
    };
    Class<?> type = Class.forName(helper,true,isolated);
    net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(new net.minecraft.item.Item());
    Object level = type.getMethod("itemLevel",net.minecraft.item.ItemStack.class,net.minecraft.entity.Entity.class).invoke(null,stack,null);
    if (!Integer.valueOf(15).equals(level)) throw new AssertionError("Automatic source without MF2");
    System.out.println("Colored dynamic-light hook loads and resolves item level with MF2 classes absent");
  }
}
