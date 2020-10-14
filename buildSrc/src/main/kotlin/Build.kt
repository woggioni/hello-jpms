fun org.gradle.api.artifacts.dsl.DependencyHandler.addLombok(vararg configurations : String) {
    configurations.forEach {
        org.gradle.kotlin.dsl.accessors.runtime.addExternalModuleDependencyTo(
                this, it, "org.projectlombok", "lombok", "1.18.12", null, null, null, null)
    }
}

fun org.gradle.api.artifacts.dsl.DependencyHandler.addJunitJupiter() {
    arrayOf("testImplementation" to "org.junit.jupiter:junit-jupiter-api:5.6.2",
            "testRuntimeOnly" to "org.junit.jupiter:junit-jupiter-engine:5.6.2").forEach { (configuration, dependency) ->
        add(configuration, dependency)
    }
}
