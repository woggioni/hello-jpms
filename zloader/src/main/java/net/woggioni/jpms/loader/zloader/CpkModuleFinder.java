package net.woggioni.jpms.loader.zloader;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class CpkModuleFinder implements ModuleFinder {
    private final Map<String, ModuleReference> modules;

    @SneakyThrows
    public CpkModuleFinder(Path cpkFile) {
        FileSystem fs = CpkURLConnection.getFileSystem(cpkFile);
        modules = Files.list(fs.getPath("lib"))
                .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".jar"))
                .flatMap(new Function<Path, Stream<ModuleReference>>(){
                    @Override
                    @SneakyThrows
                    public Stream<ModuleReference> apply(Path jarFile) {
                        FileSystem jarFs = CpkURLConnection.getFileSystem(jarFile);
                        Path moduleInfoPath = jarFs.getPath("module-info.class");
                        if(Files.exists(moduleInfoPath)) {
                            ModuleDescriptor md;
                            try (InputStream is = Files.newInputStream(moduleInfoPath)) {
                                md = ModuleDescriptor.read(is);
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
