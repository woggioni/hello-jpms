plugins {
    `java-library`
}

dependencies {
    implementation(project(":modules:A"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.10.5")
    implementation(project(":modules:json-serializer"))
}

task<Cpk>("cpk") {
    metadata {
        name("example-bundle2")
        version("1.0")
        mainClass("net.woggioni.jpms.loader.example.bundle2.Bundle")
        mainModule("net.woggioni.example.bundle2_")
    }
}