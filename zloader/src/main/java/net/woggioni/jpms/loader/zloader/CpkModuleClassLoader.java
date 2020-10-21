package net.woggioni.jpms.loader.zloader;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class CpkModuleClassLoader extends SecureClassLoader {
    private final Configuration cfg;
    private final String moduleName;
    private final String cpkFile;
    private final Path jarFile;
    private final AccessControlContext accessController;

    @Setter
    private Map<String, ModuleData> packageMap;


    private static String className2ResourceName(String className) {
        return className.replace('.', '/') + ".class";
    }

    private static String packageName(String cn) {
        int pos = cn.lastIndexOf('.');
        return (pos < 0) ? "" : cn.substring(0, pos);
    }

    public CpkModuleClassLoader(Configuration cfg,
                                String moduleName,
                                String cpkFile,
                                String jarFile,
                                ClassLoader parent) {
        super(parent);
        this.cfg = cfg;
        this.moduleName = moduleName;
        this.cpkFile = cpkFile;
        this.jarFile = CpkURLConnection.getFileSystem(Paths.get(cpkFile)).getPath(jarFile);
        this.accessController = AccessController.getContext();
    }

    @Override
    @SneakyThrows
    protected URL findResource(String resourceName) {
        if(CpkURLConnection.exists(jarFile, resourceName)) {
            return new URL(String.format("cpk://%s!%s!%s", cpkFile, jarFile.toString(), resourceName));
        } else {
            return null;
        }
    }

    @Override
    protected Enumeration<URL> findResources(String resourceName) {
        return new Enumeration<>() {
            private URL url = findResource(resourceName);

            @Override
            public boolean hasMoreElements() {
                return url != null;
            }

            @Override
            public URL nextElement() {
                URL result = url;
                url = null;
                return result;
            }
        };
    }


    @Override
    @SneakyThrows
    protected Class<?> findClass(String name) {
        log.debug("Loading class '{}' from '{}'", name, cpkFile);
        URL url = findResource(className2ResourceName(name));
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
            CodeSource codeSource = new CodeSource(jarFile.toUri().toURL(), (CodeSigner[]) null);
            return defineClass(name, buffer, 0, buffer.length, codeSource);
        } else {
            return null;
        }
    }

    @Override
    protected Class<?> findClass(String moduleName, String className) {
        Class<?> result = null;
        Optional<ResolvedModule> resolvedModuleOptional = cfg.findModule(moduleName);
        if(resolvedModuleOptional.isPresent()) {
            result = AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                @Override
                @SneakyThrows
                public Class<?> run() {
                    ResolvedModule resolvedModule = resolvedModuleOptional.get();
                    Optional<ByteBuffer> byteBufferOptional;
                    try(ModuleReader reader = resolvedModule.reference().open()) {
                        byteBufferOptional = reader.read(className2ResourceName(className));
                        if (byteBufferOptional.isPresent()) {
                            ByteBuffer byteBuffer = byteBufferOptional.get();
                            CodeSource codeSource = new CodeSource(resolvedModule.reference().location().get().toURL(), (CodeSigner[]) null);
                            try {
                                return defineClass(className, byteBuffer, codeSource);
                            } finally {
                                reader.release(byteBuffer);
                            }
                        } else {
                            return null;
                        }
                    }
                }
            }, accessController);
        }
        if(result == null) {
            result = super.findClass(moduleName, className);
        }
        return result;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String pn = packageName(className);
            if (!pn.isEmpty()) {
                sm.checkPackageAccess(pn);
            }
        }
        synchronized (getClassLoadingLock(className)) {
            Class<?> result = findLoadedClass(className);
            if (result == null) {
                ModuleData moduleData = packageMap.get(packageName(className));
                if(moduleData == null) {
                    ClassLoader parent = getParent();
                    if(parent == null) {
                        result = super.loadClass(className, resolve);
                    } else {
                        result = parent.loadClass(className);
                    }
                } else if(Objects.equals(moduleName, moduleData.name)) {
                    result = findClass(className);
                } else {
                    result = moduleData.classLoader.loadClass(className);
                }
            }
            if (result == null)
                throw new ClassNotFoundException(className);
            if (resolve)
                resolveClass(result);
            return result;
        }
    }
}
