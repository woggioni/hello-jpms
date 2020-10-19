plugins {
    application
}

dependencies {
    implementation(group = "com.beust", name = "jcommander", version = "1.78")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.11.3")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = "2.13.3")
}

application {
    mainClass.set("net.woggioni.jpms.loader.zloader.api.ZLoader")
    mainModule.set("zloader")
}

// Gradle is too dumb to understand that even if jcommander doesn't ship a JPMS-friendly jar
// (it doesn't have neither module-info.class nor an 'Automatic-Module-Name' entry in the manifest)
// it can still be used as a module using JPMS automatic module name inference from jar files,
// thus it can be safely added to the module path
java {
    modularity.inferModulePath.set(false)
}

tasks.getByName("compileJava", JavaCompile::class) {
    options.compilerArgs.add("--module-path")
    options.compilerArgs.add(classpath.asPath)
}

tasks.getByName("run", JavaExec::class) {
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
    mainClass.set("net.woggioni.jpms.loader.zloader.api.ZLoader")
    mainModule.set("zloader")
}
