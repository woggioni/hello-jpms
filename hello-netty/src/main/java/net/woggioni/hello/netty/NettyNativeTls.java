package net.woggioni.hello.netty;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;

public class NettyNativeTls {

    public static void main(String[] argv) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = NettyNativeTls.class.getResourceAsStream("/cordacadevkeys.jks")) {
            ks.load(is, "cordacadevpass".toCharArray());
        }
        KeyManagerFactory keyManagerFactory= KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(ks, "password".toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        SslContextBuilder
                .forServer(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .ciphers(Arrays.asList("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"))
                .clientAuth(ClientAuth.REQUIRE)
                .protocols("TLSv1.2")
                .sslProvider(SslProvider.OPENSSL)
                .build();
    }
}
