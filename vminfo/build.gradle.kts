import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformPluginBase.Companion.applyPlugin

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}
dependencies {
    implementation(group="org.jetbrains.kotlin", name="kotlin-stdlib-jdk8", version="1.4.0")
}

application {
    mainClass.set("net.woggioni.vminfo.VMInfoKt")
}
