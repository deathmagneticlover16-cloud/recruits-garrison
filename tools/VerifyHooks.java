import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class VerifyHooks {
  static Object value(AnnotationNode a, String key) {
    for (int i = 0; a.values != null && i < a.values.size(); i += 2)
      if (a.values.get(i).equals(key)) return a.values.get(i + 1);
    return null;
  }
  public static void main(String[] args) throws Exception {
    int hooks = 0;
    try (JarFile dependency = new JarFile(args[0]); JarFile addon = new JarFile(args[1])) {
      for (JarEntry entry : Collections.list(addon.entries())) {
        if (!entry.getName().contains("/mixin/") || !entry.getName().endsWith(".class")) continue;
        ClassNode mixin = new ClassNode(); new ClassReader(addon.getInputStream(entry)).accept(mixin, 0);
        AnnotationNode targetAnnotation = mixin.invisibleAnnotations.stream().filter(a -> a.desc.endsWith("/Mixin;")).findFirst().orElseThrow();
        for (Object targetValue : (List<?>)value(targetAnnotation, "value")) {
        Type type = (Type)targetValue;
        ClassNode target = new ClassNode();
        new ClassReader(dependency.getInputStream(dependency.getJarEntry(type.getInternalName() + ".class"))).accept(target, 0);
        for (MethodNode method : mixin.methods) {
          List<AnnotationNode> annotations = new ArrayList<>();
          if (method.visibleAnnotations != null) annotations.addAll(method.visibleAnnotations);
          if (method.invisibleAnnotations != null) annotations.addAll(method.invisibleAnnotations);
          for (AnnotationNode a : annotations) {
            if (!a.desc.endsWith("/Inject;") && !a.desc.endsWith("/ModifyVariable;")) continue;
            List<?> selectors = (List<?>)value(a, "method");
            int found = 0;
            for (Object selector : selectors) {
              String s = selector.toString();
              long matches = target.methods.stream().filter(m -> s.equals(m.name) || s.equals(m.name + m.desc)).count();
              found += matches;
              if (matches == 0 && !s.startsWith("m_")) throw new AssertionError("Missing target: " + type + ": " + s);
            }
            int required = value(a, "require") instanceof Integer i ? i : 1;
            if (found < required) throw new AssertionError("Insufficient hooks for " + method.name);
            hooks += found;
          }
        }
        }
      }
    }
    System.out.println("PASS: " + hooks + " injection target matches against Recruits 1.15.2");
  }
}
