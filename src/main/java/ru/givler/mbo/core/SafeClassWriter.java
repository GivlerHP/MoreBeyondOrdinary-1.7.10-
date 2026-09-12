package ru.givler.mbo.core;

import org.objectweb.asm.ClassWriter;

/** Creates frame-computing writers that do not load classes during LaunchWrapper transformation. */
final class SafeClassWriter extends ClassWriter {
  private SafeClassWriter() {
    super(COMPUTE_MAXS | COMPUTE_FRAMES);
  }

  static ClassWriter create() {
    return new SafeClassWriter();
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    // Coremods receive production bytecode with names such as "dh". ClassWriter's default
    // implementation uses the system class loader, which cannot resolve classes while
    // LaunchClassLoader is defining them and can recursively abort the entire class load.
    return "java/lang/Object";
  }
}
