package com.example.service.interceptor;

import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@AllArgsConstructor
public class BearerTokenAuthInterceptor implements ClientHttpRequestInterceptor {

    private final String bearerTokenValue;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        if (!headers.containsKey("Authorization")) {
            headers.setBearerAuth(this.bearerTokenValue);
        }

        return execution.execute(request, body);
    }
}
