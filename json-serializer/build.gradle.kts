plugins {
    `java-library`
}

dependencies {
    api(project(":serialization"))
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.11.3")
}


