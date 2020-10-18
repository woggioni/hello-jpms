plugins {
    `java-library`
}

dependencies {
    api(project(":modules:B"))
    compileOnly(project(":modules:serialization"))
}

task<Cpk>("cpk") {
    metadata {
        name("A")
        version("1.0")
        requirements("serialization" to "1.0")
        exports("A")
    }
}