package net.woggioni.jpms.loader.zloader.api;

import com.beust.jcommander.JCommander;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.woggioni.jpms.loader.zloader.*;
import static net.woggioni.jpms.loader.zloader.Utils.newThrowable;

import java.lang.module.*;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ZLoader {

    @Slf4j
    private static class NaiveExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        @SneakyThrows
        public void uncaughtException(Thread t, Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private static final NaiveExceptionHandler naiveExceptionHandler = new NaiveExceptionHandler();

    private static final Collector<Bundle, Map<Tuple2<String, String>, Bundle>, Map<Tuple2<String, String>, Bundle>> bundleCollector = Collector.of(
            HashMap::new,
            (map, bundle) -> {
                Tuple2<String, String> key = new Tuple2<>(bundle.getName(), bundle.getVersion());
                Bundle previous = map.put(key, bundle);
                if(previous == bundle) {
                    throw Utils.newThrowable(IllegalArgumentException.class,
                            "Duplicated bundle found with name '%s' and version '%s': '%s' and '%s'",
                            bundle.getName(), bundle.getVersion(),
                            bundle.getCpkfile().toString(), previous.getCpkfile().toString());
                }
            },
            (s1, s2) -> {
                s1.putAll(s2);
                return s1;
            },
            Collections::unmodifiableMap
    );

    @RequiredArgsConstructor
    private static class StackElement {
        public final Bundle bundle;
        public final Iterator<Tuple2<String, String>> it;

        public StackElement(Bundle bundle) {
            this.bundle = bundle;
            this.it = bundle.getRequires().iterator();
        }
        public List<ModuleLayer> children = new ArrayList<>();
    }

    public static Map<Bundle, ModuleLayer> loadBundles(Iterable<Path> bundlePath) {
        Collector<Bundle, Map<Tuple2<String, String>, Bundle>, Map<Tuple2<String, String>, Bundle>> bundleCollector = Collector.of(
                HashMap::new,
                (map, bundle) -> {
                    Tuple2<String, String> key = new Tuple2<>(bundle.getName(), bundle.getVersion());
                    Bundle previous = map.put(key, bundle);
                    if(previous == bundle) {
                        throw Utils.newThrowable(IllegalArgumentException.class,
                                "Duplicated bundle found with name '%s' and version '%s': '%s' and '%s'",
                                bundle.getName(), bundle.getVersion(),
                                bundle.getCpkfile().toString(), previous.getCpkfile().toString());
                    }
                },
                (s1, s2) -> {
                    s1.putAll(s2);
                    return s1;
                },
                Collections::unmodifiableMap
        );

        Map<Tuple2<String,String>, Bundle> bundles = Utils.iterable2Stream(bundlePath)
                .flatMap(new Function<Path, Stream<Path>>() {
                    @Override
                    @SneakyThrows
                    public Stream<Path> apply(Path path) {
                        if(Files.isDirectory(path)) {
                            return Files.list(path);
                        } else {
                            return Stream.of(path);
                        }
                    }
                }).filter(
                        path -> path.getFileName().toString().endsWith(".cpk")
                ).map(Bundle::read)
                .peek(bundle -> {
                    log.info("Found bundle '{}' with name: '{}', version: '{}'", bundle.getCpkfile().toString(), bundle.getName(), bundle.getVersion());
                })
                .collect(bundleCollector);

        Map<ModuleLayer, Bundle> layer2Bundle = new HashMap<>();
        Map<Bundle, ModuleLayer> bundle2Layer = new HashMap<>();
        Map<ModuleLayer, Map<String, ModuleData>> packageMaps = new HashMap<>();
        ArrayList<StackElement> stack = new ArrayList<>();
        for(Bundle bundle : bundles.values()) {
            StackElement stackElement = new StackElement(bundle);
            stack.add(stackElement);
            while(!stack.isEmpty()) {
                StackElement lastElement = Utils.tail(stack);
                if(lastElement.it.hasNext()) {
                    Tuple2<String, String> key = lastElement.it.next();
                    String requiredName = key._1;
                    String requiredVersion = key._2;
                    Bundle child = Optional.ofNullable(bundles.get(key))
                            .orElseThrow(() -> Utils.newThrowable(IllegalStateException.class,
                                    "Child bundle with name '%s' and version '%s' not found",
                                    requiredName, requiredVersion));
                    StackElement childStackElement = new StackElement(child);
                    stack.add(childStackElement);
                } else {
                    ModuleLayer layer = bundle2Layer.computeIfAbsent(lastElement.bundle, key -> {
                        List<String> roots = Stream.concat(
                                Utils.iterable2Stream(key.getExports()),
                                Optional.ofNullable(key.getMainModule()).stream()
                        ).collect(Collectors.toList());
                        ModuleFinder modulefinder = new CpkModuleFinder(key.getCpkfile());
                        List<ModuleLayer> parents = stackElement.children.isEmpty() ?
                                Arrays.asList(ModuleLayer.boot()) : stackElement.children;
                        Configuration cfg = Configuration.resolveAndBind(modulefinder,
                                parents.stream().map(ModuleLayer::configuration).collect(Collectors.toList()),
                                ModuleFinder.of(),
                                roots
                        );
                        ModuleLayer.Controller controller = ModuleLayer.defineModules(cfg, parents, new Function<String, ClassLoader>() {
                            private static final String SKIP = "file://";
                            @Override
                            @SneakyThrows
                            public ClassLoader apply(String moduleName) {
                                ResolvedModule resolvedModule = cfg.findModule(moduleName).get();
                                Optional<URI> location = resolvedModule.reference().location();
                                URL jarUrl = location.get().toURL();
                                String path = jarUrl.getPath();
                                if(!path.startsWith(SKIP)) {
                                    throw newThrowable(IllegalArgumentException.class,
                                            "Unknown URL '%s'", jarUrl.toString());
                                }
                                int cursor = SKIP.length();
                                cursor = path.indexOf('!', cursor);
                                int firstSeparator = cursor;
                                String cpkFilePath = path.substring(SKIP.length(), firstSeparator);
                                String jarFilePath = path.substring(firstSeparator + 1);
                                return new CpkModuleClassLoader(cfg, moduleName, cpkFilePath, jarFilePath, null);
                            }
                        });
                        ModuleLayer result = controller.layer();

                        Map<String, ModuleData> currentConfigurationPackageMap = result.modules().stream()
                                .flatMap(resolvedModule -> resolvedModule.getDescriptor().packages().stream()
                                        .map(packageName -> new Tuple2<>(packageName, resolvedModule)))
                                .collect(CollectionUtils.toUnmodifiableTreeMap(
                                        t -> t._1, t -> new ModuleData(t._2.getName(),
                                        t._2.getClassLoader())));
                        Iterable<Map<String, ModuleData>> parentMaps = result.parents().stream()
                                .filter(c -> c != ModuleLayer.boot())
                                .map(packageMaps::get)
                                .collect(Collectors.toList());
                        Map<String, ModuleData> packageMap = new DelegatingMap<>(currentConfigurationPackageMap, parentMaps);
                        for(Module m : result.modules()) {
                            ((CpkModuleClassLoader) m.getClassLoader()).setPackageMap(packageMap);
                            controller.addReads(m, m.getClassLoader().getUnnamedModule());
                        }
                        packageMaps.put(result, packageMap);

                        layer2Bundle.put(result, key);
                        for(Module module : result.modules()) {
                            for(ModuleDescriptor.Requires requirement : module.getDescriptor().requires()) {
                                Optional<Module> optionalModule = result.findModule(requirement.name());
                                if(optionalModule.isPresent()) {
                                    Module dependentModule = optionalModule.get();
                                    Bundle dependencyBundle = layer2Bundle.get(dependentModule.getLayer());
                                    if (dependencyBundle != null &&
                                        !Objects.equals(key, dependencyBundle) &&
                                        !dependencyBundle.getExports().contains(dependentModule.getName())) {
                                        throw Utils.newThrowable(ResolutionException.class,
                                                "Module '%s' from bundle '%s' requires module '%s'" +
                                                        " which is private in bundle '%s'",
                                                module.getDescriptor().toNameAndVersion(),
                                                key.getCpkfile().toString(),
                                                requirement.name(),
                                                dependencyBundle.getCpkfile().toString()
                                        );
                                    }
                                }
                            }
                        }
                        String modules = result.modules().stream().map(Module::getDescriptor).map(ModuleDescriptor::toNameAndVersion)
                                .reduce((s1, s2) -> s1 + ", " + s2).orElse("");
                        log.info("Created module layer from bundle {} with modules [{}]", key.getCpkfile().toString(), modules);
                        return result;
                    });
                    if (stack.size() > 1) {
                        StackElement parent = Utils.tail(stack, -2);
                        parent.children.add(layer);
                    }
                    Utils.pop(stack);
                }
            }
        }
        return bundle2Layer;
    }

    @SneakyThrows
    public static Set<Thread> startLayers(Map<Bundle, ModuleLayer> layers) {
        return layers.entrySet().stream()
                .filter(entry -> entry.getKey().getMainClass() != null)
                .map(new Function<Map.Entry<Bundle, ModuleLayer>, Thread>(){
                    @Override
                    @SneakyThrows
                    public Thread apply(Map.Entry<Bundle, ModuleLayer> entry) {
                        Bundle bundle = entry.getKey();
                        ModuleLayer layer = entry.getValue();
                        Module mainModule = layer.findModule(bundle.getMainModule())
                                .orElseThrow(() -> Utils.newThrowable(IllegalArgumentException.class,
                                        "Main module '%s' required by bundle '%s'",
                                        bundle.getMainModule(), bundle.getCpkfile().toString()
                                ));
                        Class<?> mainClass = Class.forName(mainModule, bundle.getMainClass());
                        if(mainClass == null) {
                            throw Utils.newThrowable(IllegalArgumentException.class,
                                    "Main class '%s' not found for bundle '%s'",
                                    bundle.getMainClass(), bundle.getCpkfile().toString());
                        }
                        if(!Runnable.class.isAssignableFrom(mainClass)) {
                            throw Utils.newThrowable(IllegalArgumentException.class,
                                    "Main class %s of bundle %s is not an instance of %s",
                                    mainClass.getName(),
                                    bundle.getCpkfile().toString(),
                                    Runnable.class.getName()
                            );
                        }
                        Constructor<Runnable> constructor = ((Class<Runnable>) mainClass).getConstructor();
                        log.info("Starting '{}' from '{}'", mainClass.getName(), bundle.getCpkfile().toString());
                        return new Thread(constructor.newInstance());
                    }
                })
                .peek(thread -> {
                    thread.setUncaughtExceptionHandler(naiveExceptionHandler);
                    thread.start();
                })
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    public static void wait(Set<Thread> threads) {
        while(!threads.isEmpty()) {
            Iterator<Thread> it = threads.iterator();
            while(it.hasNext()) {
                Thread thread = it.next();
                thread.join(500);
                if(!thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    threads.remove(thread);
                    break;
                }
            }
        }
    }

    @SneakyThrows
    public static void main(String[] argv) {
        var cliArgs = new CliArgument();
        var jc = JCommander.newBuilder()
                .addObject(cliArgs)
                .build();
        jc.parse(argv);
        if (cliArgs.help) {
            jc.usage();
            return;
        }
        URL.setURLStreamHandlerFactory(CpkStreamHandlerFactory.getInstance());

        Map<Bundle, ModuleLayer> layers = loadBundles(cliArgs.bundlePath);
        Set<Thread> threads = startLayers(layers);
        wait(threads);
    }
}
