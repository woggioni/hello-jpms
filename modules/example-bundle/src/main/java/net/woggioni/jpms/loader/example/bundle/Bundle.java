package net.woggioni.jpms.loader.example.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.woggioni.hello.jpms.a.A;
import net.woggioni.hello.jpms.a.P2d;

import java.lang.module.ModuleDescriptor;
import java.net.URL;
import java.util.Optional;


public class Bundle implements Runnable {

    @Override
    public void run() {
        P2d p1 = new P2d(1,2);
        P2d p2 = new P2d(3,4);
        System.out.println(A.distance(p1, p2));
        Module m = getClass().getModule();
        URL url = getClass().getClassLoader().getResource(ObjectMapper.class.getName().replace(".", "/") + ".class");
        System.out.printf("Module '%s' sees ObjectMapper from '%s'\n", m.getName(),  url);
        ModuleLayer layer = m.getLayer();
//        System.out.printf("Module %s %s\n", m.getDescriptor().name(), m.getDescriptor().version().map(ModuleDescriptor.Version::toString).orElse(""));
//        Optional.ofNullable(m.getDescriptor())
//                .map(mod -> mod.requires())
//                .ifPresent(mod -> mod.forEach(dependency -> {
//            System.out.printf("Dependency %s %s\n",
//                    dependency.name(),
//                    dependency.compiledVersion().map(ModuleDescriptor.Version::toString).orElse(""));
//            layer.findModule()
//        }));
        m.getLayer();
        System.out.println(p1.toString());
    }

    public static void main(String[] argv) {
        new Bundle().run();
    }
}
