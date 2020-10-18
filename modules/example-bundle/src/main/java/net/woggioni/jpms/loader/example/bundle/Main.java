package net.woggioni.jpms.loader.example.bundle;

import lombok.SneakyThrows;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        String classpath = System.getProperty("classpath.jars");
        Path[] jars = Arrays.stream(classpath.split(System.getProperty("path.separator")))
            .map(Paths::get)
            .filter(path -> path.toString().endsWith(".jar") || Files.isDirectory(path))
            .toArray(Path[]::new);
        ClassLoader cl = new URLClassLoader(Arrays.stream(jars).map(Path::toUri).map(new Function<URI,URL>() {
            @Override
            @SneakyThrows
            public URL apply(URI uri) {
                return uri.toURL();
            }
        }).toArray(URL[]::new), null);

        ModuleLayer bootLayer = ModuleLayer.boot();
        Configuration bootConfiguration = bootLayer.configuration();

        ModuleFinder moduleFinder = ModuleFinder.of(jars);
        Configuration cfg = Configuration.resolve(
                moduleFinder,
                Collections.singletonList(bootConfiguration),
                ModuleFinder.of(),
                Collections.singletonList("example_bundle"));
        ModuleLayer newLayer = ModuleLayer.defineModulesWithOneLoader(cfg, Collections.singletonList(bootLayer), cl).layer();
        Module module = newLayer.findModule("example_bundle").get();
        Class<?> cls = Class.forName(module, Bundle.class.getName());
        System.out.println(newLayer);
    }
}
