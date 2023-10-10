package com.github.zly2006.reden.asm;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;

public class FabricLoaderInjector {
    private final ClassLoader knotClassLoader;
    private final Method defMethod;
    private final Method auMethod;

    public FabricLoaderInjector(ClassLoader knotClassLoader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        this.knotClassLoader = knotClassLoader;
        var kclClass = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClassLoader");
        if (!kclClass.isInstance(knotClassLoader)) {
            throw new IllegalArgumentException("knotClassLoader must be an instance of KnotClassLoader");
        }
        defMethod = kclClass.getMethod("defineClassFwd", String.class, byte[].class, int.class, int.class, CodeSource.class);
        defMethod.setAccessible(true);
        auMethod = kclClass.getMethod("addUrlFwd", URL.class);
        auMethod.setAccessible(true);
    }

    public Class<?> defineClass(@Nullable String name, byte[] b, int off, int len, @Nullable CodeSource cs) throws InvocationTargetException, IllegalAccessException {
        return (Class<?>) defMethod.invoke(knotClassLoader, name, b, off, len, cs);
    }

    public Class<?> defineClass(byte[] byteArray) throws InvocationTargetException, IllegalAccessException {
        return defineClass(null, byteArray, 0, byteArray.length, null);
    }

    public void addURL(URL url) throws InvocationTargetException, IllegalAccessException {
        auMethod.invoke(knotClassLoader, url);
    }

    public Class<?> defineClass(ClassNode node) throws InvocationTargetException, IllegalAccessException {
        ClassWriter cw = new ClassWriter(3);
        node.accept(cw);
        return defineClass(cw.toByteArray());
    }
}
