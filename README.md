## Overview
This is a basic prototype of a container dynamically running java modules packaged in `.cpk` 
archives (which is simply a jar of jars).

Each cpk archive has a `lib` folder containing some jar files, each jar maps to a JPMS module
(an automatic module will be created if the jars lacks a `module-info.class`)

Each cpk file contains a metadata file in `META-INF/cpk.json` with the following structure
```json
{
  "name" : "bundle-name",
  "version" : "1.0",
  "main-class" : "main.class.full.Name",
  "main-module" : "name.of.module.containing.amin.class",
  "exports":  ["jpms.module1.name", "jpms.module2.name"],
  "requirements" : {
    "bundle-name2" : "2.0",
    "bundle-name3" : "3.0"
  }
}
```

From now on the term *bundle* will be used to indicate a collection of JPMS modules that needs to be available
to the same application. 

If modules are to be packaged in jars, a bundle is also a collection of jar files, one for each module.
A cpk archive is a convenient format to package a set of jars in a single file.

A bundle can have a *main-class*, that has to implement the `java.lang.Runnable` interface, 
if it has a main class it will also need to define a *main module* which is simply the name of the JPMS module
that contains its *main-class*.
 

A *bundle* doesn't need to be fully self-contained, 
it can delegate some of its functionality to other bundles (which implies that they will be required to be available at runtime).

A *bundle* by default doesn't expose any of its modules to other *bundles*, 
but can choose to do so if it wants to. A module contained in a bundle that is
made available to other bundles is called an *exported module*.

If a bundle `bundle1` that contains module `A` depends on bundle `bundle2` 
that also contains module `A` and `A` is an *exported module* of `bundle2`, then `bundle1`
and `bundle2` will use their own copy of `A` (modules in `bundle1` that "requires" `A` will read `bundle1` own copy of `A` and
modules in `bundle2` that "requires" `A` will read `bundle2` own copy of `A`). 

If `A` is removed from `bundle1`, then the modules in `bundle1` that require `A` will use 
`bundle2` own copy of `A`, this is because `A` is an *exported module* in `bundle2`. If that was
not the case, loading `bundle1` would result in a runtime failure because `bundle1` is trying
to use a private module from another bundle.

## Demo
The current code defines 5 different JPMS modules `A`, `B`, `example_bundle`, `serialization` and `json-serializer`.
- `serialization` simply exposes a service an interface
```java
public interface Serializer {
    String serialize(Object o);
}
```

- `json-serializer` contains the implementation of the `serialization` service, 
which internally uses `com.fasterxml.jackson.databind` but doesn't expose anything

- `B` that defines the `square` method, which is implemented in a private package `net.woggioni.jpms.loader.b.impl` 
(not exported), also part of the same module
```java
import net.woggioni.jpms.loader.b.impl.BImpl;

public class B {
    public static int square(int n) {
        return BImpl.square(n);
    }
}
```


- `A` that defines `net.woggioni.jpms.loader.a.P2d` which uses the `Serializer` service from the `serialization` module to print its
stringified version
```java
import lombok.Getter;
import net.woggioni.jpms.loader.serialization.Serializer;

import java.util.ServiceLoader;

public class P2d {

    private static Serializer serializer;

    static {
        serializer = ServiceLoader.load(P2d.class.getModule().getLayer(), Serializer.class).findFirst().orElseThrow(() ->
                new RuntimeException(String.format("Unable to load service '%s'", Serializer.class.getName()))
        );
    }

    @Getter
    private final int x, y;

    public P2d(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return serializer.serialize(this);
    }
}
```
- `example_bundle` that contains a `Bundle` class that implements the `java.lang.Runnable` interface
 and is used as the bundle main class, it simply creates instaces of `P2d`, compute their distances 
 and converts them to string. It also depends on `com.fasterxml.jackson.databind` and will print on the stdout
 frm which archive it is loading the `P2d` and `ObjectMapper` classes
```java
public class Bundle implements Runnable {

    @Override
    @SneakyThrows
    public void run() {
        P2d p1 = new P2d(1,2);
        P2d p2 = new P2d(3,4);
        System.out.printf("Distance from %s to %s is %d\n", p1.toString(), p2.toString(), A.distance(p1, p2));
        ClassLoadingUtils.debugClass(P2d.class.getName(), getClass());
        ClassLoadingUtils.debugClass(ObjectMapper.class.getName(), getClass());
    }
}
```

These modules are deployed in 3 different bundles:

`json-serializer-1.0-SNAPSHOT.cpk` contains modules
- `com.fasterxml.jackson.annotation@2.11.3`
- `json.serializer@1.0-SNAPSHOT`
- `serialization@1.0-SNAPSHOT`
- `com.fasterxml.jackson.databind@2.11.3`
- `com.fasterxml.jackson.core@2.11.3`
 
`A-1.0-SNAPSHOT.cpk` contains modules
- `B@1.0-SNAPSHOT`
- `A@1.0-SNAPSHOT`


`example-bundle-1.0-SNAPSHOT.cpk` contains modules:
- `com.fasterxml.jackson.databind@2.10.5`
- `com.fasterxml.jackson.core@2.10.5`
- `com.fasterxml.jackson.annotation@2.10.5`
- `example_bundle@1.0-SNAPSHOT`
 
To run demo use the `:zloader:run` gradle task which deploys and starts the `serializer`, `A` and `example-bundle` cpk packages.

## Build
Use the `:zloader:distTar` gradle task to package the loader in its own archive, it can than be used
invoking
```bash
zloader-1.0-SNAPSHOT/bin/zloader -b bundle1.cpk -b bundle2.cpk -b folder_containing_cpk_files
```

## Creating additional bundles
There is a convenient cpk gradle task provided for ease bundle file creation 
(particularly to create the `META-INF/cpk.json` file):

```kotlin
task<Cpk>("cpk") {
    metadata {
        name("example-bundle")
        version("1.0")
        mainClass("net.woggioni.jpms.loader.example.bundle.Bundle")
        mainModule("example_bundle")
        requirements("serialization" to "1.0")
        requirements("A" to "1.0")
        exports("example_bundle")
    }
}
```
by default it will package the current project artifact and all of its runtime dependencies in a file named
```${project.name}-${project.version}.cpk```

## Implementation details
A new module layer is created of each bundle and the parents of this layer will be the layers 
of the required bundles specified in the `cpk.json`. Bundles that do not have any requirement will be children 
of the boot layer directly.

Every bundle is loaded with a different classloader that can only load classes from that specific cpk archive, 
that classloader is a direct child of the bootstrap classloader.
JPMS doesn't provide a way to limit access to modules in a resolved configuration, 
if an implementation package (say `google.guava`) is included because some modules needs it, it will be available 
to every module in the same configuration. 
This means that even if a *bundle* could ship with its own version of Guava 
(and in that case it will only be able to use that module even if some other module 
in a dependent bundle is also using another version of `google.guava`), 
it could exclude it from the cpk archive and use the one that some other dependent bundle included in a parent layer.
To avoid leaking private modules I created the 'export' abstraction from bundle which is checked
at configuration resolution time (when creating a new module layer, starting the bundle). 
If in the resolved configuration a bundle is using modules from other bundles, 
those modules needs to be marked as *exported* in the owning bundle `cpk.json`, 
otherwise an exception is thrown at startup time.

## Limitations

### Loading classes from nested jars directly
Cpk files aren't extracted, and no temporary directory is used, classes are loaded directly from inside cpk archives 
(nested jars). Implementing this feature proved way harder than I expected it to be and has some limitations
(multi-release jars are currently not supported). 
This is because `java.util.jar.JarFile` is tied to `java.io.File` instead of `java.nio.file.Path`, 
which binds it to a file on the filesystem, there is no way to re-use its code for a file mapped by a `java.nio.file.Path`.

As an example, `jdk.internal.module.ModulePath` which is the default implementation of `ModuleFinder` 
and contains a lot of useful and needed functionalities (like the code to list java packages available in a jar file) 
understands jar files in terms of `java.util.jar.JarFile`, which also ties it to `java.io.File` 
(a file in native OS filesystem, not `java.nio.file.Filesystem`). 
