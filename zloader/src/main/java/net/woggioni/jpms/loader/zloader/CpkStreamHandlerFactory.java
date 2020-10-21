package net.woggioni.jpms.loader.zloader;

import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class CpkURLConnection extends URLConnection {

    private static final Map<Path, FileSystem> fileSystemCache = Collections.synchronizedMap(new HashMap<>());

    public static final boolean exists(Path jarFile, String resourceName) {
        return Files.exists(getFileSystem(jarFile).getPath(resourceName));
    }

    static FileSystem getFileSystem(Path path) {
        return fileSystemCache.computeIfAbsent(path, new Function<>() {
            @Override
            @SneakyThrows
            public FileSystem apply(Path path) {
                return FileSystems.newFileSystem(path, CpkURLConnection.class.getClassLoader());
            }
        });
    }

    private final Path resourcePath;

    protected CpkURLConnection(URL url) {
        super(url);
        CpkURL cpkURL = CpkURL.from(url);
        FileSystem cpkFs = getFileSystem(Paths.get(cpkURL.cpkFilePath));
        FileSystem jarFs = getFileSystem(cpkFs.getPath(cpkURL.jarFilePath));
        resourcePath = jarFs.getPath(cpkURL.resourceName);
    }

    @Override
    public void connect() {
    }

    @Override
    @SneakyThrows
    public InputStream getInputStream() {
        return Files.newInputStream(resourcePath);
    }
}

class CpkURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) {
        return new CpkURLConnection(url);
    }
}

public class CpkStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final Object lock = new Object();
    private static CpkStreamHandlerFactory instance;

    public static CpkStreamHandlerFactory getInstance() {
        CpkStreamHandlerFactory result = instance;
        if (result == null) {
            synchronized (lock) {
                result = instance;
                if (result == null) {
                    result = new CpkStreamHandlerFactory();
                    instance = result;
                }
            }
        }
        return result;
    }

    static {
        CpkStreamHandlerFactory factory = getInstance();
        factory.registerHandler("cpk", new CpkURLStreamHandler());
    }

    private final Map<String, URLStreamHandler> protocolMap;

    private CpkStreamHandlerFactory() {
        protocolMap = Collections.synchronizedMap(new TreeMap<>());
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return protocolMap.get(protocol);
    }

    public void registerHandler(String protocol, URLStreamHandler handler) {
        protocolMap.put(protocol, handler);
    }
}
