package net.corda.jpms.loader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CliArgument {
    @Parameter(names = {"-p", "--path"},
            description = "The path to the folder containing the bundle files")
    public Path bundlePath = Paths.get("bundles");

    @Parameter(names = {"-h", "--help"}, help = true)
    public boolean help = false;
}

public class Loader {

    public static void main(String[] argv) {
        var cliArgs = new CliArgument();
        var jc = JCommander.newBuilder()
                .addObject(cliArgs)
                .build();
        jc.parse(argv);
        if(cliArgs.help) {
            jc.usage();
            return;
        }
    }
}
