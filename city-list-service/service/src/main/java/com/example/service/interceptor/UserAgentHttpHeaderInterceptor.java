package com.example.service.interceptor;

import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@AllArgsConstructor
public class UserAgentHttpHeaderInterceptor implements ClientHttpRequestInterceptor {
    private static final String USER_AGENT_HEADER_NAME = "User-Agent";
    private final String userAgentHeaderValue;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(USER_AGENT_HEADER_NAME, userAgentHeaderValue);
        return execution.execute(request, body);
    }
}
