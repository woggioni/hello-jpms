plugins {
    `java-library`
    application
}

dependencies {
    implementation(project(":modules:A"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.10.5")
}

application {
    mainClass.set("net.woggioni.jpms.loader.example.bundle.Bundle")
    mainModule.set("net.woggioni.jpms.loader.example.bundle")
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
        mainModule("net.woggioni.jpms.loader.example.bundle")
        requirements("serialization" to "1.0")
    }
}