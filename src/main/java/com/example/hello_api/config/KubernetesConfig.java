package com.example.hello_api.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;

import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class KubernetesConfig {

    @Bean
    public ApiClient kubernetesApiClient() throws Exception {

        ApiClient client = Config.defaultClient();

        // Keep it as the default client as well.
        Configuration.setDefaultApiClient(client);

        return client;
    }
}