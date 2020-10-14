plugins {
    id("org.javamodularity.moduleplugin") version("1.7.0") apply(false)
    id("org.jetbrains.kotlin.jvm") version("1.4.0") apply(false)
}
allprojects {
    apply<JavaLibraryPlugin>()

    val compileJavaTask = tasks.withType(JavaCompile::class.java)["compileJava"].apply {
        sourceCompatibility = "11"
        targetCompatibility = "11"
//        include { it.name == "module-info.java" }
    }

//    val sourceSetContainer = property("sourceSets") as SourceSetContainer
//    sourceSetContainer["main"].java.exclude { it.name == "module-info.java" }
//    sourceSetContainer.register("moduleInfo") {
//        java {
//            setSrcDirs(sourceSetContainer["main"].java.sourceDirectories)
//            include { it.name == "module-info.java" }
//            println(destinationDirectory.asFile.get())// = sourceSetContainer["main"].java.destinationDirectory
//            compileClasspath = compileJavaTask.classpath
//            compiledBy(tasks.register("compileModuleInfo", JavaCompile::class.java) {
//                sourceCompatibility = "11"
//                targetCompatibility = "11"
////                exclude { it.name == "module-info.java" }
//                source = sourceSetContainer["main"].java
//                classpath = compileJavaTask.classpath
////            destinationDirectory.set(compileJavaTask.destinationDirectory)
//            }, AbstractCompile::getDestinationDirectory)
//        }
//        sourceSetContainer["main"].java.sourceDirectories.forEach(java::setSrcDirs)
//        java.source(sourceSetContainer["main"].java.sourceDirectories)
//    }

//
//    val compileJavaSource = tasks.register("compileJavaSource", JavaCompile::class.java) {
//        sourceCompatibility = "8"
//        targetCompatibility = "8"
//        exclude { it.name == "module-info.java" }
//        source = sourceSetContainer["main"].java
//        classpath = compileJavaTask.classpath
//        destinationDirectory.set(compileJavaTask.destinationDirectory)
//    }
//
//    compileJavaTask.dependsOn(compileJavaSource)
//
    dependencies {
        addLombok("compileOnly", "annotationProcessor", "testCompileOnly", "testAnnotationProcessor")
        addJunitJupiter()
    }

    tasks.withType(JavaCompile::class.java) {
        options.javaModuleVersion.set(provider { version as String })
    }

    configure<JavaPluginExtension> {
        modularity.inferModulePath.set(true)
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    val test by tasks.getting(Test::class) {
        useJUnitPlatform()
    }

    version = "1.0-SNAPSHOT"
}
