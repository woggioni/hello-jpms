package net.corda.jpms.loader;

import lombok.SneakyThrows;

import java.lang.module.ModuleFinder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class CpkModuleFinder {

    @SneakyThrows
    public static ModuleFinder from(Path cpkPath) {
        FileSystem fs = FileSystems.newFileSystem(cpkPath, ClassLoader.getSystemClassLoader());
        Path[] jars = Files.list(fs.getPath("lib"))
            .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".jar"))
            .toArray(Path[]::new);
        return ModuleFinder.of(jars);
    }
}
