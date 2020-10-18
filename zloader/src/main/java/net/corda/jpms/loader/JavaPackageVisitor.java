package net.corda.jpms.loader;

import java.io.IOException;
import java.lang.module.InvalidModuleDescriptorException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;

class JavaPackageVisitor implements FileVisitor<Path> {

    private static final Set<String> RESERVED = Set.of(
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while",
            "true",
            "false",
            "null",
            "_"
    );

    private static boolean isJavaIdentifier(String str) {
        if (str.isEmpty() || RESERVED.contains(str))
            return false;

        int first = Character.codePointAt(str, 0);
        if (!Character.isJavaIdentifierStart(first))
            return false;

        int i = Character.charCount(first);
        while (i < str.length()) {
            int cp = Character.codePointAt(str, i);
            if (!Character.isJavaIdentifierPart(cp))
                return false;
            i += Character.charCount(cp);
        }

        return true;
    }

    private static boolean isTypeName(String name) {
        int next;
        int off = 0;
        while ((next = name.indexOf('.', off)) != -1) {
            String id = name.substring(off, next);
            if (!isJavaIdentifier(id))
                return false;
            off = next+1;
        }
        String last = name.substring(off);
        return isJavaIdentifier(last);
    }

    private static boolean isPackageName(String name) {
        return isTypeName(name);
    }

    private static final String META_INF = "META-INF/";

    private static final String META_INF_VERSIONS = META_INF + "versions/";


    private final TreeSet<String> packageNames = new TreeSet<>();
    public Set<String> getPackageNames() {
        return packageNames;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String filepath = file.normalize().toString();
        if(filepath.endsWith(".class")) {
            if(filepath.startsWith(META_INF_VERSIONS)) {
                throw new IllegalArgumentException("Multi release jars are not supported");
            }
            int index = filepath.lastIndexOf("/");
            if (index == -1) {
                if (filepath.endsWith(".class") && !filepath.equals("module-info.class")) {
                    String msg = filepath + " found in top-level directory"
                            + " (unnamed package not allowed in module)";
                    throw new InvalidModuleDescriptorException(msg);
                }
            } else {
                String packageName = filepath.substring(0, index).replace('/', '.');
                if(isPackageName(packageName)) {
                    packageNames.add(packageName);
                }
            }
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
