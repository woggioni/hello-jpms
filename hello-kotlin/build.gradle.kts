plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

dependencies {
    implementation(group="org.jetbrains.kotlin", name="kotlin-stdlib-jdk8", version="1.4.0")
    implementation(project(":A"))
}

application {
    mainClass.set("net.woggioni.hello.jpms.Hello")
}

