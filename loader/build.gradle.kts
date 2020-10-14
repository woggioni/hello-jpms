plugins {
    `java-library`
    application
}

dependencies {
    implementation(group="com.beust", name="jcommander", version="1.78")
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version= "2.11.2")
}

application {
    mainClass.set("net.corda.jpms.loader.Loader")
    mainModule.set("net.corda.jpms.loader")
}
