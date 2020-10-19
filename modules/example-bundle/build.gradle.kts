plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":modules:A"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.10.5")
}

tasks.withType<JavaExec>().configureEach {
    (sequenceOf(sourceSets.main.get().java.outputDir) + configurations.named("default").get().files.asSequence())
            .map {it.toString()}
            .joinToString(System.getProperty("path.separator")).let {
                systemProperty("classpath.jars", it)
            }
}

task<Cpk>("cpk") {
    metadata {
        name("example-bundle")
        version("1.0")
        mainClass("net.woggioni.jpms.loader.example.bundle.Bundle")
        mainModule("example_bundle")
        requirements("serialization" to "1.0")
        requirements("A" to "1.0")
    }
}