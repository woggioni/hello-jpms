plugins {
    `java-library`
}

java {
    modularity.inferModulePath.set(true)
}

//task<Jar>("cpk") {
//    manifest {
//        attributes("Cpk-Name" to "serialization")
//        attributes("Cpk-Version" to "1.0")
//    }
//    archiveExtension.set("cpk")
//    into("/lib") {
//        entryCompression = ZipEntryCompression.STORED
//        configurations["default"].resolve().forEach {
//            from(it)
//        }
//    }
//}
