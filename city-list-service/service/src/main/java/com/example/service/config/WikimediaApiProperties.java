package com.example.service.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "wikimedia-api-properties")
public class WikimediaApiProperties {

    private final String authToken;
    private final String userAgent;
    private final int readTimeout;
    private final int connectionTimeout;

}
