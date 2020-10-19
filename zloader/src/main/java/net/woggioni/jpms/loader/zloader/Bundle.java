package net.woggioni.jpms.loader.zloader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.woggioni.jpms.loader.zloader.Utils.jsonGet;
import static net.woggioni.jpms.loader.zloader.Utils.newThrowable;

@Data
@RequiredArgsConstructor
public class Bundle {
    private static final ObjectMapper om = new ObjectMapper();
    private static final ObjectReader reader = om.reader().without(JsonParser.Feature.AUTO_CLOSE_SOURCE);

    private final Path cpkfile;

    @EqualsAndHashCode.Include
    private final String name;

    @EqualsAndHashCode.Include
    private final String version;

    private final String mainClass;

    private final String mainModule;

    private final Set<String> exports;

    private final Iterable<Tuple2<String, String>> requires;

    @SneakyThrows
    public static Bundle read(Path cpkFile) {
        InputStream is = Files.newInputStream(cpkFile);
        BufferedInputStream bis = new BufferedInputStream(is);
        try(ZipInputStream zis = new ZipInputStream(bis)) {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                else if (Objects.equals("META-INF/cpk.json", zipentry.getName())) {
                    JsonNode json = reader.readTree(zis);
                    String name = jsonGet(json, "name")
                            .map(JsonNode::asText)
                            .orElseThrow(() -> newThrowable(IllegalArgumentException.class,
                                    "Required field 'name' not found in 'META_INF/cpk.json' inside '%s'", cpkFile.toString()));
                    String version = jsonGet(json, "version")
                            .map(JsonNode::asText)
                            .orElseThrow(() -> newThrowable(IllegalArgumentException.class,
                                    "Required field 'version' not found in 'META_INF/cpk.json' inside '%s'", cpkFile.toString()));
                    String mainClass = jsonGet(json, "main-class")
                            .filter(Predicate.not(JsonNode::isNull))
                            .map(JsonNode::asText).orElse(null);
                    String mainModule = jsonGet(json, "main-module")
                            .filter(Predicate.not(JsonNode::isNull))
                            .map(JsonNode::asText).orElse(null);
                    if(mainClass == null ^ mainModule == null) {
                        throw newThrowable(IllegalArgumentException.class,
                                "Fields 'main-class' and 'main-module' in 'META_INF/cpk.json' " +
                                        "inside '%s' must either be both null or both not-null", cpkFile.toString());
                    }
                    Collection<Tuple2<String, String>> requirements = jsonGet(json,"requirements").stream()
                            .map(node -> ((ObjectNode) node))
                            .map(ObjectNode::fields)
                            .flatMap(Utils::iterator2Stream)
                            .map(entry -> new Tuple2<>(entry.getKey(), entry.getValue().asText()))
                            .collect(Collectors.toUnmodifiableList());

                    Set<String> exports = jsonGet(json,"exports").stream()
                            .map(node -> ((ArrayNode) node))
                            .map(ArrayNode::elements)
                            .flatMap(Utils::iterator2Stream)
                            .map(JsonNode::asText)
                            .collect(CollectionUtils.toUnmodifiableTreeSet());
                    return new Bundle(cpkFile, name, version, mainClass, mainModule, exports, requirements);
                }
            }
            throw new IllegalArgumentException(String.format("File 'META-INF/cpk.json' not found in '%s'", cpkFile.toString()));
        }
    }
}
