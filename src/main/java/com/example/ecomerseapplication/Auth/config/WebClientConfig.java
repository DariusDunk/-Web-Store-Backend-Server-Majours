package com.example.ecomerseapplication.Auth.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Value("${keycloak.server-url}")
    private String tokenAddress;

    public WebClientConfig() throws SSLException {
    }

    //TODO KOGATO NAPRAVI6 KONTEINERIZACIQATA MAHNI SY6TESTVUVA6TATA KONFIGURACIQ I VYRNI KOMENTIRANATA

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(tokenAddress)
                .build();
    }
//
//    @Bean
//    public WebClient keycloakWebClient() throws SSLException {
//
//        SslContext sslContext = SslContextBuilder
//                .forClient()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                .build();
//
//        HttpClient httpClient = HttpClient.create()
//                .secure(ssl -> ssl.sslContext(sslContext));
//
//        return WebClient.builder()
//                .baseUrl(tokenAddress)
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .build();
//    }



}
