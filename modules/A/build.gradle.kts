plugins {
    `java-library`
}

dependencies {
    api(project(":modules:B"))
    compileOnly(project(":modules:serialization"))
}