// source of public-jar-1.2-all.jar
// part of the reden project (https://redenmc.com)
// copyright (c) zly2006, MIT License
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar jar-publicizer.jar <input-jar> <output-jar> [--field]");
            System.exit(1);
        } else {
            String jarFileName = args[0];
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration<JarEntry> entries = jarFile.entries();
            new File(args[1]).getParentFile().mkdirs();
            boolean publicFields = Arrays.asList(args).contains("--field");
            FileOutputStream jarOutputStream = new FileOutputStream(args[1]);
            JarOutputStream jar = new JarOutputStream(jarOutputStream);

            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream entryInputStream = jarFile.getInputStream(entry);
                if (entry.getName().endsWith(".class")) {
                    jar.putNextEntry(new JarEntry(entry.getName()));
                    jar.write(modifyClassAccess(entryInputStream, publicFields));
                } else {
                    if (entry.getName().endsWith(".SF") || entry.getName().endsWith(".DSA") || entry.getName().endsWith(".RSA")) {
                        System.out.println("Skipping signature file " + entry.getName());
                        continue;
                    }
                    if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        System.out.println("Skipping manifest file " + entry.getName());
                        continue;
                    }
                    jar.putNextEntry(new JarEntry(entry.getName()));
                    // copy the file
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while((bytesRead = entryInputStream.read(buffer)) != -1) {
                        jar.write(buffer, 0, bytesRead);
                    }
                }
            }

            jar.close();
            System.out.println("All classes in the JAR have been modified to public.");
        }
    }

    private static byte[] modifyClassAccess(InputStream inputStream, boolean publicFields) throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                if ((access & 1) == 0) {
                    System.out.println("Modifying class " + name);
                }

                super.visit(version, (access | 1) & -3 & -5, name, signature, superName, interfaces);
            }

            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return super.visitMethod((access | 1) & -3 & -5, name, descriptor, signature, exceptions);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (publicFields) {
                    return super.visitField((access | 1) & -3 & -5, name, descriptor, signature, value);
                } else {
                    return super.visitField(access, name, descriptor, signature, value);
                }
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
