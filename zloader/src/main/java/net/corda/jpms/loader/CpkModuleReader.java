package net.corda.jpms.loader;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.module.ModuleReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CpkModuleReader implements ModuleReader {
    private final Path cpkFile;
    private final Path jarFile;
    private final FileSystem jarFs;

    @Override
    @SneakyThrows
    public Optional<URI> find(String name) {
        Path resource = jarFs.getPath(name);
        if(Files.exists(resource)) {
            URL url = new URL(String.format("cpk://%s!%s!%s", cpkFile.toString(), jarFile.toString(), name));
            return Optional.of(url.toURI());
        } else {
            return Optional.empty();
        }
    }

    @Override
    @SneakyThrows
    public Stream<String> list() {
        return Files.walk(jarFs.getPath("/")).filter(Predicate.not(Files::isDirectory)).map(Path::toString);
    }

    @Override
    @SneakyThrows
    public void close() {
        jarFs.close();
    }
}
