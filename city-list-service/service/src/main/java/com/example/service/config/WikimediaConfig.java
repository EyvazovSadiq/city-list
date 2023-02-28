package com.example.service.config;

import com.example.service.interceptor.BearerTokenAuthInterceptor;
import com.example.service.interceptor.UserAgentHttpHeaderInterceptor;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WikimediaConfig {

    @Bean(name = "wikimediaApiRestTemplate")
    public RestTemplate wikimediaApiRestTemplate(
            RestTemplateBuilder builder,
            WikimediaApiProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectionTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .additionalInterceptors(
                        new BearerTokenAuthInterceptor(properties.getAuthToken()),
                        new UserAgentHttpHeaderInterceptor(properties.getUserAgent())
                )
                .build();
    }
}
