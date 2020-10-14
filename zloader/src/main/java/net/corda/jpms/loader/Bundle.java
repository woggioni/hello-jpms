package net.corda.jpms.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.corda.jpms.loader.tuple.Tuple2;

import java.nio.file.Path;

@Data
@RequiredArgsConstructor
public class Bundle {
    private final Path cpkfile;

    @EqualsAndHashCode.Include
    private final String name;

    @EqualsAndHashCode.Include
    private final String version;

    private final String mainClass;

    private final String mainModule;

    private final Iterable<Tuple2<String, String>> requires;
}
