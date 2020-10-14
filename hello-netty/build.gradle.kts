plugins {
    application
}

dependencies {
    val nettyVersion = "4.1.46.Final"
//    val artemisVersion = "2.6.2"
//    val qpidVersion = "0.54.0"
    // https://mvnrepository.com/artifact/io.netty/netty-handler
    implementation(group = "io.netty", name = "netty-handler", version = nettyVersion)
    implementation(group = "io.netty", name = "netty-codec", version = nettyVersion)
    implementation(group = "io.netty", name = "netty-codec-http", version = nettyVersion)
    implementation(group = "io.netty", name = "netty-tcnative-boringssl-static", version = "2.0.29.Final")
//    implementation(group = "org.apache.activemq", name = "artemis-server", version = artemisVersion)
//    implementation(group = "org.apache.activemq", name = "artemis-jms-client", version = artemisVersion)
//    implementation(group = "org.apache.activemq", name = "artemis-amqp-protocol", version = artemisVersion)
//    implementation(group = "org.apache.qpid", name = "qpid-jms-client", version = qpidVersion)
}

application {
    mainClass.set("net.woggioni.hello.netty.NettyNativeTls")
}