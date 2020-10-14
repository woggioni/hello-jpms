package net.corda.jpms.loader;

import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CpkClassLoader extends ClassLoader {

    private final Path cpkFile;
    private Map<String, FileSystem> jars;

    @SneakyThrows
    public CpkClassLoader(Path cpkFile, ClassLoader parent) {
        super(parent);
        this.cpkFile = cpkFile;
        jars = new TreeMap<>();
        FileSystem fs = FileSystems.newFileSystem(cpkFile, getParent());
        Files.list(fs.getPath("lib"))
            .filter(entry -> Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(".jar"))
            .forEach(new Consumer<Path>() {
                @Override
                @SneakyThrows
                public void accept(Path path) {
                    jars.put(path.toString(), FileSystems.newFileSystem(path, parent));
                }
            });
    }

    @SneakyThrows
    private static <T> Stream<T> listZipEntries(Path zipFile, BiFunction<ZipEntry, InputStream, T> callback) {
        InputStream is = Files.newInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(is);
        ZipInputStream zis = new ZipInputStream(bis);
        Iterator<T> it = new Iterator<T>() {
            private ZipEntry ze = zis.getNextEntry();
            @Override
            public boolean hasNext() {
                return ze != null;
            }

            @Override
            @SneakyThrows
            public T next() {
                T result = callback.apply(ze, zis);
                ze = zis.getNextEntry();
                return result;
            }
        };
        return Utils.iterator2Stream(it);
    }

    private Stream<URL> resourceStream(String resourceName) {
        return jars.entrySet().stream().flatMap(new Function<Map.Entry<String, FileSystem>, Stream<URL>>() {
            @Override
            @SneakyThrows
            public Stream<URL> apply(Map.Entry<String, FileSystem> entry) {
                String jar = entry.getKey();
                FileSystem fs = entry.getValue();
                Path path = fs.getPath(resourceName);
                if(Files.exists(path)) {
                    return Stream.of(new URL(String.format("cpk://%s!%s!%s", cpkFile.toString(), jar, resourceName)));
                } else {
                    return Stream.of();
                }
            }
        });
    }

    @Override
    protected URL findResource(String resourceName) {
        return resourceStream(resourceName).findFirst().orElse(null);
    }

    @Override
    protected Enumeration<URL> findResources(String resourceName) {
        return new Enumeration<>() {
            private final Iterator<URL> it = resourceStream(resourceName).iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    @SneakyThrows
    protected Class<?> findClass(String name) {
        URL url = getResource(name.replace('.', '/') + ".class");
        if(url != null) {
            byte[] buffer = new byte[0x10000];
            try(InputStream is = url.openStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                while(true) {
                    int read = is.read(buffer);
                    if(read < 0) break;
                    baos.write(buffer, 0, read);
                }
                buffer = baos.toByteArray();
            }
            return defineClass(name, buffer, 0, buffer.length);
        } else {
            return null;
        }
    }
}
