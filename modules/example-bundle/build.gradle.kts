plugins {
    `java-library`
    application
}

dependencies {
    compileOnly(project(":modules:A"))
//    implementation(project(":modules:json-serializer"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.10.5")
}

application {
    mainClass.set("net.woggioni.jpms.loader.example.bundle.Main")
    mainModule.set("example_bundle")
}


tasks.withType<JavaExec>().configureEach {
    (sequenceOf(sourceSets.main.get().java.outputDir) + configurations.named("default").get().files.asSequence())
            .map {it.toString()}
            .joinToString(System.getProperty("path.separator")).let {
                systemProperty("classpath.jars", it)
            }
}

//task<Jar>("cpk") {
//    val jarTask = tasks.named("jar", Jar::class).get()
//    dependsOn(jarTask)
//    entryCompression = ZipEntryCompression.STORED
//    manifest {
//        attributes("Main-Class" to "net.woggioni.hello.jpms.example.bundle.Bundle")
//    }
//    archiveExtension.set("cpk")
//    into("META-INF") {
//        from("cpk/cpk.json")
//    }
//    into("/lib") {
//        configurations["default"].resolve().forEach {
//            from(it)
//        }
//        from(jarTask.archiveFile)
//    }
//}

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