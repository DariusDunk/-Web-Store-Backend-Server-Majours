package com.example.ecomerseapplication.TensorFlowConfig;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TFConfig {
    @Bean(destroyMethod = "shutdown")
    public ManagedChannel tfChannel(
            @Value("${tensorflow.serving.grpc-address}") String host,
            @Value("${tensorflow.serving.grpc-port}") int port
    ) {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }
}
