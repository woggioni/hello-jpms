plugins {
    id("org.javamodularity.moduleplugin") version("1.7.0") apply(false)
    id("org.jetbrains.kotlin.jvm") version("1.4.0") apply(false)
}
allprojects {
    apply<JavaLibraryPlugin>()

    val sourceSetContainer = property("sourceSets") as SourceSetContainer

    val compileJavaTask = tasks.withType(JavaCompile::class.java)["compileJava"].apply {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        include { it.name == "module-info.java" }
    }

    val compileJavaSource = tasks.register("compileJavaSource", JavaCompile::class.java) {
        sourceCompatibility = "8"
        targetCompatibility = "8"
        exclude { it.name == "module-info.java" }
        source = sourceSetContainer["main"].java
        classpath = compileJavaTask.classpath
        destinationDirectory.set(compileJavaTask.destinationDirectory)
    }

    compileJavaTask.dependsOn(compileJavaSource)

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    tasks.withType(JavaCompile::class.java) {
        options.javaModuleVersion.set(provider { version as String })
    }

    configure<JavaPluginExtension> {
        modularity.inferModulePath.set(true)
    }

    val test by tasks.getting(Test::class) {
        useJUnitPlatform()
    }

    version = "1.0-SNAPSHOT"
}