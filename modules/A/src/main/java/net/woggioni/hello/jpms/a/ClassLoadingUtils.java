package net.woggioni.hello.jpms.a;

import lombok.SneakyThrows;

import java.net.URL;

public class ClassLoadingUtils {
    @SneakyThrows
    public static void debugClass(String className, Class<?> caller) {
        Module m = caller.getModule();
        Class<?> cls = Class.forName(className, false, caller.getClassLoader());
        ClassLoader loader = cls.getClassLoader();
        URL url = loader.getResource(className.replace('.', '/') + ".class");
        if(url == null) {
            System.out.printf("Module '%s' cannot see class '%s'\n", m.getName(), className);
        } else {
            System.out.printf("Module '%s' sees '%s' from '%s'\n", m.getName(),  className, url);
        }
    }

}
