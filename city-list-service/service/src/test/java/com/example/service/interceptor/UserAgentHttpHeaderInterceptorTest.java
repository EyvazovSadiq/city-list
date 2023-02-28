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
class UserAgentHttpHeaderInterceptorTest {

    private static final String USER_AGENT_HEADER_KEY = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "APPLICATION_NAME";

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private ClientHttpRequestExecution clientHttpRequestExecution;

    @InjectMocks
    private UserAgentHttpHeaderInterceptor testObj;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @BeforeEach
    void setUp() {
        Field applicationNameField = ReflectionUtils.findRequiredField(UserAgentHttpHeaderInterceptor.class, "userAgentHeaderValue");
        ReflectionUtils.setField(applicationNameField, testObj, USER_AGENT_HEADER_VALUE);
    }

    @Test
    void interceptor_calledWidthX_addsCorrectAcceptHeader() throws IOException {
        // given
        given(httpRequest.getHeaders()).willReturn(httpHeaders);

        // when
        testObj.intercept(httpRequest, null, clientHttpRequestExecution);

        // then
        assertThat(httpHeaders.get(USER_AGENT_HEADER_KEY).get(0)).isEqualTo(USER_AGENT_HEADER_VALUE);
    }

}
