package net.corda.jpms.loader;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class CpkModuleFinder implements ModuleFinder {
    private final Map<String, ModuleReference> modules;

    @SneakyThrows
    public CpkModuleFinder(Path cpkFile) {
        FileSystem fs = FileSystems.newFileSystem(cpkFile, ClassLoader.getSystemClassLoader());
        modules = Files.list(fs.getPath("lib"))
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".jar"))
                .flatMap(new Function<Path, Stream<ModuleReference>>(){
                    @Override
                    @SneakyThrows
                    public Stream<ModuleReference> apply(Path jarFile) {
                        FileSystem jarFs = FileSystems.newFileSystem(jarFile, ClassLoader.getSystemClassLoader());
                        Path moduleInfoPath = jarFs.getPath("module-info.class");
                        if(Files.exists(moduleInfoPath)) {
//                            JavaPackageVisitor javaPackageVisitor = new JavaPackageVisitor();
//                            Files.walkFileTree(jarFs.getPath("."), javaPackageVisitor);
                            ModuleDescriptor md;
                            try (InputStream is = Files.newInputStream(moduleInfoPath)) {
                                md = ModuleDescriptor.read(is);
//                                md = ModuleDescriptor.read(is, javaPackageVisitor::getPackageNames);
                            }
                            log.info("Module '{}' requires [{}]", md.name(),
                                    md.requires().stream()
                                        .map(requires -> String.format("'%s'", requires.toString()))
                                        .collect(Collectors.joining(", ")));
                            return Stream.of(new ModuleReference(md, jarFile.toUri()) {
                                @Override
                                public ModuleReader open() {
                                    return new CpkModuleReader(cpkFile, jarFile, jarFs);
                                }
                            });
                        } else {
                            return Stream.empty();
                        }
                    }
                }).collect(CollectionUtils.toUnmodifiableTreeMap((ModuleReference ref) -> ref.descriptor().name(), Function.identity()));
    }

    @Override
    public Optional<ModuleReference> find(String name) {
        return Optional.ofNullable(modules.get(name));
    }

    @Override
    public Set<ModuleReference> findAll() {
        return new HashSet<>(modules.values());
    }
}
