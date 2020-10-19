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

    static final Map<Path, FileSystem> cpkFilesystems = new HashMap<>();

    private final FileSystem fs;
    private final String jarPath;
    private final String className;

    protected CpkURLConnection(URL url) {
        super(url);
        String path = url.getPath();
        int cursor = path.indexOf('!');
        if (cursor < 0) throw new IllegalArgumentException(String.format("Invalid URL '%s'", url.toString()));
        int firstSeparator = cursor;
        cursor = path.indexOf('!', cursor + 1);
        if (cursor < 0) throw new IllegalArgumentException(String.format("Invalid URL '%s'", url.toString()));
        int secondSeparator = cursor;
        String cpkPath = path.substring(0, firstSeparator);
        fs = cpkFilesystems.computeIfAbsent(Paths.get(cpkPath), new Function<>() {
            @Override
            @SneakyThrows
            public FileSystem apply(Path path) {
                return FileSystems.newFileSystem(path, CpkURLConnection.class.getClassLoader());
            }
        });
        jarPath = path.substring(firstSeparator + 1, secondSeparator);
        className = path.substring(secondSeparator + 1);
    }

    @SneakyThrows
    private static InputStream getZipEntry(Path zipFile, String entryName) {
        InputStream is = Files.newInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(is);
        ZipInputStream zis = new ZipInputStream(bis);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if(Objects.equals(entryName, ze.getName()))
                return zis;
        }
        throw new IllegalArgumentException(String.format("Entry wih name '%s' not found in '%s'", entryName, zipFile.toString()));
    }

    @Override
    public void connect() {}

    @Override
    public InputStream getInputStream() {
        return getZipEntry(fs.getPath(jarPath), className);
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
