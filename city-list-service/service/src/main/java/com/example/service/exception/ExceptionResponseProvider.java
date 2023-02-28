package com.example.service.exception;

import com.example.service.dto.ErrorResponse;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
public class ExceptionResponseProvider {

    private ExceptionResponseProvider() {
    }

    private static final String LOG_PATTERN = "{} occurred, exception message = {}";

    protected static ResponseEntity<ErrorResponse> logAndGetErrorResponse(Exception ex, HttpStatus responseStatusCode) {
        var errorMessages = Collections.singletonList(ex.getMessage());
        var errorResponse = logAndBuildErrorResponse(ex, responseStatusCode, errorMessages);
        return new ResponseEntity<>(errorResponse, responseStatusCode);
    }

    protected static ResponseEntity<ErrorResponse> logAndGetValidationErrorResponse(MethodArgumentNotValidException ex) {
        var errorMessages = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
        var errorResponse = logAndBuildErrorResponse(ex, HttpStatus.BAD_REQUEST, errorMessages);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private static ErrorResponse logAndBuildErrorResponse(Exception ex, HttpStatus responseStatusCode, List<String> errorMessages) {
        if (responseStatusCode.is4xxClientError()) {
            log.warn(LOG_PATTERN, ex.getClass().getSimpleName(), errorMessages);
        } else {
            log.error(LOG_PATTERN, ex.getClass().getSimpleName(), errorMessages);
        }
        return ErrorResponse.builder()
                .type(responseStatusCode.toString())
                .errorMessages(errorMessages)
                .build();
    }
}
