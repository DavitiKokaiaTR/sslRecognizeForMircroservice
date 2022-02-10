package com.server.app;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class ServerConfig
{

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(SslContextFactory.Server sslContextFactory ) {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();

        JettyServerCustomizer jettyServerCustomizer = server -> {
            ServerConnector serverConnector = new ServerConnector(server, sslContextFactory);
            serverConnector.setPort(9007);
            server.setConnectors(new Connector[]{serverConnector});
        };
        factory.setServerCustomizers(Collections.singletonList(jettyServerCustomizer));

        return factory;
    }

}
