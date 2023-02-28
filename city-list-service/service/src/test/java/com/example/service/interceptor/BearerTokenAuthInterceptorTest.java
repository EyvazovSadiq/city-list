package com.example.service.interceptor;

import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class BearerTokenAuthInterceptorTest {

    private static final String BEARER_TOKEN_VALUE = "authToken";

    private HttpHeaders httpHeaders = new HttpHeaders();

    @InjectMocks
    private BearerTokenAuthInterceptor authInterceptor;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private ClientHttpRequestExecution clientHttpRequestExecution;

    @BeforeEach
    void setUp() {
        Field bearerTokenField = ReflectionUtils.findRequiredField(BearerTokenAuthInterceptor.class, "bearerTokenValue");
        ReflectionUtils.setField(bearerTokenField, authInterceptor, BEARER_TOKEN_VALUE);
    }

    @Test
    void intercept_calledWithoutAuthHeader_addsAuthHeader() throws IOException {
        // given
        given(httpRequest.getHeaders()).willReturn(httpHeaders);

        // when
        authInterceptor.intercept(httpRequest, null, clientHttpRequestExecution);

        // then
        assertThat(httpHeaders.get("Authorization").get(0)).isEqualTo("Bearer " + BEARER_TOKEN_VALUE);
    }

    @Test
    void intercept_calledWithAuthHeader_doesNotAddAuthHeader() throws IOException {
        httpHeaders.setBearerAuth("authValue");

        // given
        given(httpRequest.getHeaders()).willReturn(httpHeaders);

        // when
        authInterceptor.intercept(httpRequest, null, clientHttpRequestExecution);

        // then
        assertThat(httpHeaders.get("Authorization").get(0)).isEqualTo("Bearer authValue");
    }

}
