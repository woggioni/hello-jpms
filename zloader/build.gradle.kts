plugins {
    application
}

dependencies {
    implementation(group = "com.beust", name = "jcommander", version = "1.80")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.11.3")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = "2.13.3")
}

application {
    mainClass.set("net.corda.jpms.loader.Loader")
    mainModule.set("net.corda.jpms.loader")
}

tasks.create("go", JavaExec::class.java) {
    val arguments = ArrayList<String>()
    arrayOf(":modules:example-bundle", ":modules:json-serializer", ":modules:A").forEach {
        val cpkTask = tasks.getByPath("$it:cpk") as Cpk
        dependsOn(cpkTask)
        for (file in cpkTask.outputs.files) {
            arguments.add("-b")
            arguments.add(file.toString())
        }
    }
    args = arguments
    dependsOn("compileJava")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.corda.jpms.loader.ZLoader")
    mainModule.set("net.corda.jpms.loader")
}