package net.woggioni.jpms.loader.zloader.api;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class CliArgument {
    @Parameter(names = {"-b", "--bundle-path"},
            description = "The path to the folder containing the application bundles",
            descriptionKey = "BUNDLE_PATH",
            converter = PathConverter.class
    )
    public List<Path> bundlePath = Collections.singletonList(Paths.get("bundles"));

    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help = false;
}
