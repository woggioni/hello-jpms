plugins {
    application
}

dependencies {
    implementation(project(":A"))
}

application {
    mainClass.set("net.woggioni.hello.jpms.Library")
    mainModule.set("net.woggioni.hello.jpms")
}

task<Jar>("cpk") {
    manifest {
        attributes("Main-Class" to "com.baeldung.fatjar.Application")
    }
    archiveExtension.set("cpk")
    into("/lib") {
        entryCompression = ZipEntryCompression.STORED
        configurations["default"].resolve().forEach {
            from(it)
        }
    }
}