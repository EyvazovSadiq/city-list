package com.example.service.exception;


import com.example.service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GenericExceptionHandler {

    @ExceptionHandler(CityNotFoundException.class)
    ResponseEntity<ErrorResponse> cityNotFoundExceptionHandler(CityNotFoundException ex) {
        return ExceptionResponseProvider.logAndGetErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    ResponseEntity<ErrorResponse> imageNotFoundExceptionHandler(ImageNotFoundException ex) {
        return ExceptionResponseProvider.logAndGetErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidPageNumberException.class)
    ResponseEntity<ErrorResponse> invalidPageNumberExceptionHandler(InvalidPageNumberException ex) {
        return ExceptionResponseProvider.logAndGetErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        return ExceptionResponseProvider.logAndGetValidationErrorResponse(ex);
    }
}
