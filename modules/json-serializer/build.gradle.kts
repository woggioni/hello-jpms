plugins {
    `java-library`
}

dependencies {
    api(project(":modules:serialization"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.11.3")
}

task<Cpk>("cpk") {
    metadata {
        name("serialization")
        version("1.0")
        exports("serialization")
        exports("json.serializer")
    }
}