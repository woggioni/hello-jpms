package net.woggioni.jpms.loader.zloader;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

public class CpkStreamHandlerFactoryTest {

    @TempDir
    static Path temporaryDirectory;

    @SneakyThrows
    private static void write2Output(OutputStream os, InputStream inputStream, byte[] buffer) {
        while (true) {
            int read = inputStream.read(buffer);
            if (read < 0) break;
            os.write(buffer, 0, read);
        }
    }

    @SneakyThrows
    private static Map.Entry<Long, Long>  computeSizeAndCrc32(InputStream inputStream, byte[] buffer) {
        CRC32 crc32 = new CRC32();
        long sz = 0;
        while (true) {
            int read = inputStream.read(buffer);
            if (read < 0) break;
            sz += read;
            crc32.update(buffer, 0, read);
        }
        return new AbstractMap.SimpleEntry<>(sz, crc32.getValue());
    }

    @SneakyThrows
    byte[] computeMD5(InputStream is) {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        try(DigestInputStream dis = new DigestInputStream(is, md5)) {
            byte[] buffer = new byte[0x10000];
            while (true) {
                int read = dis.read(buffer);
                if (read < 0) break;
            }
            return dis.getMessageDigest().digest();
        }
    }

    @Test
    @SneakyThrows
    public void test() {
        Class.forName(CpkStreamHandlerFactory.class.getName());
        String className = "/" + Test.class.getName().replace('.', '/') + ".class";
        URL res = getClass().getResource(className);
        String path = res.getPath();
        int colon = path.indexOf(':');
        int separator = path.indexOf('!');
        Path jarPath = Paths.get(path.substring(colon + 1, separator));
        Files.copy(jarPath, temporaryDirectory.resolve(jarPath.getFileName()));
        Path cpkPath = temporaryDirectory.resolve("test.cpk");
        String subJarPath = "lib/" + jarPath.getFileName();
        byte[] buffer = new byte[0x10000];
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(cpkPath))) {
            ZipEntry zipEntry = new ZipEntry(subJarPath);
            zipEntry.setMethod(ZipEntry.STORED);
            Map.Entry<Long, Long> sizeAndCrc;
            try(InputStream is = Files.newInputStream(jarPath)) {
                sizeAndCrc = computeSizeAndCrc32(is, buffer);
            }
            zipEntry.setSize(sizeAndCrc.getKey());
            zipEntry.setCompressedSize(sizeAndCrc.getKey());
            zipEntry.setCrc(sizeAndCrc.getValue());
            jos.putNextEntry(zipEntry);
            try(InputStream is = Files.newInputStream(jarPath)) {
                write2Output(jos, is, buffer);
            }
        }
        ClassLoader cpkClassLoader = new CpkClassLoader(cpkPath, null);
        Assertions.assertNotNull(Class.forName(Test.class.getName(), true, cpkClassLoader));
        ClassLoader thisClassLoader = getClass().getClassLoader();
        String resourceName= Test.class.getName().replace('.', '/') + ".class";
        byte[] digest1 = computeMD5(thisClassLoader.getResourceAsStream(resourceName));
        byte[] digest2 = computeMD5(cpkClassLoader.getResourceAsStream(resourceName));
        Assertions.assertArrayEquals(digest1, digest2);
    }
}
