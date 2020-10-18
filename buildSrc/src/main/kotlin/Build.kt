import org.gradle.api.tasks.bundling.ZipEntryCompression
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import java.io.File
import java.io.FileOutputStream

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.named


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

data class CpkMetaData(
    val name : String,
    val version : String,
    val mainClass : String?,
    val mainModule : String?,
    val requirements : List<Pair<String, String>>,
    val exports : List<String>
) : java.io.Serializable {
    class Builder {
        private var name : String? = null
        private var version : String? = null
        private var mainClass : String? = null
        private var mainModule : String? = null
        private var requirements : ArrayList<Pair<String, String>> = ArrayList()
        private var exports : ArrayList<String> = ArrayList()

        fun name(name : String) = let {
            this.name = name
            this
        }

        fun version(version : String) = let {
            this.version = version
            this
        }

        fun mainClass(mainclass : String) = let {
            this.mainClass = mainclass
            this
        }

        fun mainModule(mainModule : String) = let {
            this.mainModule = mainModule
            this
        }

        fun exports(export: String) = let {
            exports.add(export)
            this
        }

        fun exports(exports: Iterable<String>) = let {
            this.exports = ArrayList<String>().apply {
                for(export in exports) {
                    add(export)
                }
            }
            this
        }

        fun requirements(requirements: Iterable<Pair<String, String>>) = let {
            this.requirements = ArrayList<Pair<String, String>>().apply {
                for(requirement in requirements) {
                    add(requirement)
                }
            }
            this
        }

        fun requirements(requirement: Pair<String, String>) = let {
            requirements.add(requirement)
            this
        }

        fun build() = let {
            require(name != null) {"'name' must not be null"}
            require(version != null) {"'version' must not be null"}
            CpkMetaData(
                    name=name!!,
                    version = version!!,
                    requirements = requirements,
                    exports = exports,
                    mainClass = mainClass,
                    mainModule = mainModule)
        }
    }

    companion object {
        fun builder() = Builder()
    }
}

open class Cpk : Jar () {

    open class WriteMetadataFile: DefaultTask() {


        private object CpkMetaDataSerializer : StdSerializer<CpkMetaData>(CpkMetaData::class.java) {
            override fun serialize(
                    value: CpkMetaData,
                    jgen: JsonGenerator,
                    provider: SerializerProvider?) {
                jgen.writeStartObject()
                jgen.writeObjectField(CpkMetaData::name.name, value.name)
                jgen.writeObjectField(CpkMetaData::version.name, value.version)
                value.mainClass?.let { jgen.writeObjectField("main-class", it) }
                value.mainModule?.let { jgen.writeObjectField("main-module", it) }
                if(value.exports.isNotEmpty()) {
                    jgen.writeArrayFieldStart(CpkMetaData::exports.name)
                    for(export in value.exports) {
                        jgen.writeString(export)
                    }
                    jgen.writeEndArray()
                }
                if(value.requirements.isNotEmpty()) {
                    jgen.writeObjectFieldStart(CpkMetaData::requirements.name)
                    for (requirement in value.requirements) {
                        jgen.writeObjectField(requirement.first, requirement.second)
                    }
                    jgen.writeEndObject()
                }
            }
        }

        companion object {
            private val om = ObjectMapper().apply {
                val module = SimpleModule()
                module.addSerializer(CpkMetaDataSerializer)
                registerModule(module)
            }
            val writer = om.writer()
                    .with(SerializationFeature.INDENT_OUTPUT)
                    .without(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        }

        @Input
        var metadata : Provider<CpkMetaData> = project.objects.property(CpkMetaData::class.java)

        @OutputFile
        var destination : File? = null

        @TaskAction
        fun create() {
            metadata.get().let {
                FileOutputStream(destination!!).use { stream ->
                    writer.writeValue(stream, it)
                }
            }
        }
    }

    private var metadata : Property<CpkMetaData>

    init {
        entryCompression = ZipEntryCompression.STORED
        val jarTask = project.tasks.named("jar", Jar::class).get()
        dependsOn(jarTask)
        archiveExtension.set("cpk")
        val metaDataFile = File(project.buildDir, "cpk.json")
        into("META-INF") {
                from(metaDataFile)
        }
        into("/lib") {
            entryCompression = ZipEntryCompression.STORED
            project.configurations.get("default").resolve().forEach {
                from(it)
            }
            from(jarTask.archiveFile)
        }
        metadata = project.objects.property(CpkMetaData::class.java)
        dependsOn(project.tasks.create("writeCpkMetadata", WriteMetadataFile::class.java) {
            destination = metaDataFile
            metadata = this@Cpk.metadata
        })
    }

    fun metadata(closure : CpkMetaData.Builder.() -> Unit) {
        metadata.set(CpkMetaData.builder().apply(closure).build())
    }
}