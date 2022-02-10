package com.server.app;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.JettySslUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

@Configuration
public class SSLConfig
{

    @Bean
    public X509ExtendedKeyManager keyManager(SSLFactory sslFactory) {
        return sslFactory.getKeyManager().orElseThrow();
    }


    @Bean
    public SSLSessionContext serverSessionContext(SSLFactory sslFactory) {
        return sslFactory.getSslContext().getServerSessionContext();
    }

    @Bean
    public X509ExtendedTrustManager trustManager(SSLFactory sslFactory) {
        return sslFactory.getTrustManager().orElseThrow();
    }


    @Bean
    public SSLFactory sslFactory(@Value("${server.ssl.keystore-path}") String keyStorePath) throws URISyntaxException
    {

        return SSLFactory.builder()
                .withSwappableIdentityMaterial()
                .withIdentityMaterial(keyStorePath, "server-app".toCharArray())
                .withSwappableTrustMaterial()
                .withTrustMaterial(keyStorePath, "server-app".toCharArray())
                .withNeedClientAuthentication()
                .build();
    }

    @Bean
    public SslContextFactory.Server sslContextFactory(SSLFactory sslFactory) {
        return JettySslUtils.forServer(sslFactory);
    }





}
