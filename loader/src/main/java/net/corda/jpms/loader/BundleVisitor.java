package net.corda.jpms.loader;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class BundleVisitor {
//    private final Path bundlePath;

    private static class ModuleVisitor implements FileVisitor<Path> {
        final ArrayList<Path> modules = new ArrayList<>();

        public Path[] getModules() {
            return (Path[]) modules.toArray();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if(file.getFileName().toString().endsWith(".jar")) {
                modules.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

//    @SneakyThrows
//    Stream<Bundle> foo() {
//        Configuration bootConfiguration = ModuleLayer.boot().configuration();
//        FileSystem fs = FileSystems.newFileSystem(bundlePath, getClass().getClassLoader());
//        return Utils.iterable2Stream(fs.getRootDirectories()).map(root -> {
//            final var visitor = new ModuleVisitor();
//            Files.walkFileTree(root, visitor);
//            final var finder = ModuleFinder.of(visitor.getModules());
//            Configuration configuration = bootConfiguration.resolve(finder, ModuleFinder.of(), Collections.emptySet());
//            Files.newBufferedReader("META-INF")
//        });
//    }
}
